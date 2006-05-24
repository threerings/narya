//
// $Id: SpotSceneDirector.java 3890 2006-02-24 19:51:11Z mthomas $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.whirled.spot.client {

import com.threerings.util.ResultListener;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.client.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.ScenedBodyObject;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.spot.data.ClusteredBodyObject;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotScene;

/**
 * Extends the standard scene director with facilities to move between
 * locations within a scene.
 */
public class SpotSceneDirector extends BasicDirector
    implements Subscriber, AttributeChangeListener
{
    private static const log :Log = Log.getLog(SpotSceneDirector);

    /**
     * Creates a new spot scene director with the specified context and
     * which will cooperate with the supplied scene director.
     *
     * @param ctx the active client context.
     * @param locdir the location director with which we will be
     * cooperating.
     * @param scdir the scene director with which we will be cooperating.
     */
    public function SpotSceneDirector (
            ctx :WhirledContext, locdir :LocationDirector, scdir :SceneDirector)
    {
        super(ctx);

        _wctx = ctx;
        _scdir = scdir;

        // wire ourselves up to hear about leave place notifications
        locdir.addLocationObserver(new LocationAdapter(null,
            function (place :PlaceObject) :void {
                handleDeparture();
            }));
    }

    /**
     * Configures this spot scene director with a chat director, with
     * which it will coordinate to implement cluster chatting.
     */
    public function setChatDirector (chatdir :ChatDirector) :void
    {
        _chatdir = chatdir;
    }

    /**
     * Returns our current location unless we have a location change
     * pending, in which case our pending location is returned.
     */
    public function getIntendedLocation () :Location
    {
        return (_pendingLoc != null) ? _pendingLoc : _location;
    }

    /**
     * Requests that this client move to the location specified by the
     * supplied portal id. A request will be made and when the response is
     * received, the location observers will be notified of success or
     * failure.
     *
     * @return true if the request was issued, false if it was rejected by
     * a location observer or because we have another request outstanding.
     */
    public function traversePortal (
            portalId :int,
            rl :com.threerings.util.ResultListener = null) :Boolean
    {
        // look up the destination scene and location
        var scene :SpotScene = (_scdir.getScene() as SpotScene);
        if (scene == null) {
            log.warning("Requested to traverse portal when we have " +
                "no scene [portalId=" + portalId + "].");
            return false;
        }

        // sanity check the server's notion of what scene we're in with
        // our notion of it
        var sceneId :int = _scdir.getScene().getId();
        var sbobj :ScenedBodyObject = 
            (_wctx.getClient().getClientObject() as ScenedBodyObject);
        if (sceneId != sbobj.getSceneId()) {
            log.warning("Client and server differ in opinion of what scene " +
                "we're in [sSceneId=" + sbobj.getSceneId() +
                ", cSceneId=" + sceneId + "].");
            return false;
        }

        // find the portal they're talking about
        var dest :Portal = scene.getPortal(portalId);
        if (dest == null) {
            log.warning("Requested to traverse non-existent portal " +
                "[portalId=" + portalId + ", portals=" + scene.getPortals() +
                "].");
            return false;
        }

        // prepare to move to this scene (sets up pending data)
        if (!_scdir.prepareMoveTo(dest.targetSceneId, rl)) {
            log.info("Portal traversal vetoed by scene director " +
                 "[portalId=" + portalId + "].");
            return false;
        }

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        var sceneVer :int = 0;
        var pendingModel :SceneModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVer = pendingModel.version;
        }

        // issue a traversePortal request
        log.info("Issuing traversePortal(" +
             sceneId + ", " + dest + ", " + sceneVer + ").");
        _sservice.traversePortal(
            _wctx.getClient(), sceneId, portalId, sceneVer, _scdir);
        return true;
    }

    /**
     * Issues a request to change our location within the scene to the
     * specified location.
     *
     * @param loc the new location to which to move.
     * @param listener will be notified of success or failure. Most client
     * entities find out about location changes via changes to the
     * occupant info data, but the initiator of a location change request
     * can be notified of its success or failure, primarily so that it can
     * act in anticipation of a successful location change (like by
     * starting a sprite moving toward the new location), but backtrack if
     * it finds out that the location change failed.
     */
    public function changeLocation (
            loc :Location, listener :com.threerings.util.ResultListener) :void
    {
        // refuse if there's a pending location change or if we're already
        // at the specified location
        if (loc.equivalent(_location)) {
            log.info("Not going to " + loc + "; we're at " + _location +
                 " and we're headed to " + _pendingLoc + ".");
            if (listener != null) {
                // This isn't really a failure, it's just a no-op.
                listener.requestCompleted(_location);
            }
            return;
        }

        if (_pendingLoc != null) {
            log.info("Not going to " + loc + "; we're at " + _location +
                " and we're headed to " + _pendingLoc + ".");
            if (listener != null) {
                // Already moving, best thing to do is ignore it.
                listener.requestCompleted(_pendingLoc);
            }
            return;
        }

        var scene :SpotScene = (_scdir.getScene() as SpotScene);
        if (scene == null) {
            log.warning("Requested to change locations, but we're not " +
                "currently in any scene [loc=" + loc + "].");
            if (listener != null) {
                listener.requestFailed(new Error("m.cant_get_there"));
            }
            return;
        }

        var sceneId :int = _scdir.getScene().getId();
        log.info("Sending changeLocation request [scid=" + sceneId +
             ", loc=" + loc + "].");

        _pendingLoc = (loc.clone() as Location);
        var clist :ConfirmAdapter = new ConfirmAdapter(
            function () :void {
                _location = _pendingLoc;
                _pendingLoc = null;
                if (listener != null) {
                    listener.requestCompleted(_location);
                }
            },
            function (reason :String) :void {
                _pendingLoc = null;
                if (listener != null) {
                    listener.requestFailed(new Error(reason));
                }
            });
        _sservice.changeLocation(_wctx.getClient(), sceneId, loc, clist);
    }

    /**
     * Issues a request to join the cluster associated with the specified
     * user (starting one if necessary).
     *
     * @param froid the bodyOid of another user; the calling user will
     * be made to join the target user's cluster.
     * @param listener will be notified of success or failure.
     */
    public function joinCluster (
            froid :int, listener :com.threerings.util.ResultListener) :void
    {
        var scene :SpotScene = (_scdir.getScene() as SpotScene);
        if (scene == null) {
            log.warning("Requested to join cluster, but we're not " +
                "currently in any scene [froid=" + froid + "].");
            if (listener != null) {
                listener.requestFailed(new Error("m.cant_get_there"));
            }
            return;
        }

        log.info("Joining cluster [friend=" + froid + "].");

        _sservice.joinCluster(_wctx.getClient(), froid, new ConfirmAdapter(
            function () :void {
                if (listener != null) {
                    listener.requestCompleted(null);
                }
            },
            function (reason :String) :void {
                if (listener != null) {
                    listener.requestFailed(new Error(reason));
                }
            }));
    }

    /**
     * Sends a chat message to the other users in the cluster to which the
     * location that we currently occupy belongs.
     *
     * @return true if a cluster speak message was delivered, false if we
     * are not in a valid cluster and refused to deliver the request.
     */
    public function requestClusterSpeak (
            message :String, mode :int = ChatCodes.DEFAULT_MODE) :Boolean
    {
        // make sure we're currently in a scene
        var scene :SpotScene = (_scdir.getScene() as SpotScene);
        if (scene == null) {
            log.warning("Requested to speak to cluster, but we're not " +
                "currently in any scene [message=" + message + "].");
            return false;
        }

        // make sure we're part of a cluster
        if (_self.getClusterOid() <= 0) {
            log.info("Ignoring cluster speak as we're not in a cluster " +
                 "[cloid=" + _self.getClusterOid() + "].");
            return false;
        }

        message = _chatdir.filter(message, null, true);
        if (message != null) {
            _sservice.clusterSpeak(_wctx.getClient(), message, mode);
        }
        return true;
    }

    // documentation inherited from interface
    public function objectAvailable (object :DObject) :void
    {
        clearCluster(false);
        var oid :int = object.getOid();
        if (oid != _self.getClusterOid()) {
            // we got it too late, just unsubscribe
            var omgr :DObjectManager = _wctx.getDObjectManager();
            omgr.unsubscribeFromObject(oid, this);
        } else {
            // it's our new cluster!
            _clobj = object;
            if (_chatdir != null) {
                _chatdir.addAuxiliarySource(object,
                    SpotCodes.CLUSTER_CHAT_TYPE);
            }
        }
    }

    // documentation inherited from interface
    public function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        log.warning("Unable to subscribe to cluster chat object " +
            "[oid=" + oid + ", cause=" + cause + "].");
    }

    // documentation inherited from interface
    public function attributeChanged (event :AttributeChangedEvent) :void
    {
        if (event.getName() == _self.getClusterField() &&
                event.getValue() != event.getOldValue()) {
            maybeUpdateCluster();
        }
    }

    // documentation inherited
    public override function clientDidLogon (event :ClientEvent) :void
    {
        super.clientDidLogon(event);

        var clientObj :ClientObject = event.getClient().getClientObject();
        if (clientObj is ClusteredBodyObject) {
            // listen to the client object
            clientObj.addListener(this);
            _self = (clientObj as ClusteredBodyObject);

            // we may need to subscribe to a cluster due to session resumption
            maybeUpdateCluster();
        }
    }

    // documentation inherited
    public override function clientObjectDidChange (event :ClientEvent) :void
    {
        super.clientObjectDidChange(event);

        // listen to the client object
        var clientObj :ClientObject = event.getClient().getClientObject();
        clientObj.addListener(this);
        _self = (clientObj as ClusteredBodyObject);
    }

    // documentation inherited
    public override function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);

        // clear out our business
        _location = null;
        _pendingLoc = null;
        _sservice = null;
        clearCluster(true);

        // stop listening to the client object
        event.getClient().getClientObject().removeListener(this);
        _self = null;
    }

    // documentation inherited
    protected override function fetchServices (client :Client) :void
    {
        _sservice = (client.requireService(SpotService) as SpotService);
    }

    /**
     * Clean up after a few things when we depart from a scene.
     */
    protected function handleDeparture () :void
    {
        // clear out our last known location id
        _location = null;
    }

    /**
     * Checks to see if our cluster has changed and does the necessary
     * subscription machinations if necessary.
     */
    protected function maybeUpdateCluster () :void
    {
        var cloid :int = _self.getClusterOid();
        if ((_clobj == null && cloid <= 0) ||
            (_clobj != null && cloid == _clobj.getOid())) {
            // our cluster didn't change, we can stop now
            return;
        }

        // clear out any old cluster object
        clearCluster(false);

        // if there's a new cluster object, subscribe to it
        if (_chatdir != null && cloid > 0) {
            var omgr :DObjectManager = _wctx.getDObjectManager();
            // we'll wire up to the chat director when this completes
            omgr.subscribeToObject(cloid, this);
        }
    }

    /**
     * Convenience routine to unwire chat for and unsubscribe from our
     * current cluster, if any.
     *
     * @param force clear the cluster even if we're still apparently in it.
     */
    protected function clearCluster (force :Boolean) :void
    {
        if (_clobj != null &&
                (force || (_clobj.getOid() != _self.getClusterOid()))) {
            if (_chatdir != null) {
                _chatdir.removeAuxiliarySource(_clobj);
            }
            var omgr :DObjectManager = _wctx.getDObjectManager();
            omgr.unsubscribeFromObject(_clobj.getOid(), this);
            _clobj = null;
        }
    }

    /** The active client context. */
    protected var _wctx :WhirledContext;

    /** Access to spot scene services. */
    protected var _sservice :SpotService;

    /** The scene director with which we are cooperating. */
    protected var _scdir :SceneDirector;

    /** A casted reference to our clustered body object. */
    protected var _self :ClusteredBodyObject;

    /** A reference to the chat director with which we coordinate. */
    protected var _chatdir :ChatDirector;

    /** The location we currently occupy. */
    protected var _location :Location;

    /** The location to which we have an outstanding change location
     * request. */
    protected var _pendingLoc :Location;

    /** The cluster chat object for the cluster we currently occupy. */
    protected var _clobj :DObject;
}
}
