//
// $Id: SpotSceneDirector.java,v 1.34 2004/03/10 16:20:04 mdb Exp $

package com.threerings.whirled.spot.client;

import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.spot.Log;
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
    implements SpotCodes, Subscriber, AttributeChangeListener
{
    /**
     * Creates a new spot scene director with the specified context and
     * which will cooperate with the supplied scene director.
     *
     * @param ctx the active client context.
     * @param locdir the location director with which we will be
     * cooperating.
     * @param scdir the scene director with which we will be cooperating.
     */
    public SpotSceneDirector (WhirledContext ctx, LocationDirector locdir,
                              SceneDirector scdir)
    {
        super(ctx);

        _ctx = ctx;
        _scdir = scdir;

        // wire ourselves up to hear about leave place notifications
        locdir.addLocationObserver(new LocationAdapter() {
            public void locationDidChange (PlaceObject place) {
                // we need to clear some things out when we leave a place
                handleDeparture();
            }
        });
    }

    /**
     * Configures this spot scene director with a chat director, with
     * which it will coordinate to implement cluster chatting.
     */
    public void setChatDirector (ChatDirector chatdir)
    {
        _chatdir = chatdir;
    }

    /**
     * Returns our current location unless we have a location change
     * pending, in which case our pending location is returned.
     */
    public Location getIntendedLocation ()
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
    public boolean traversePortal (int portalId)
    {
        return traversePortal(portalId, null);
    }

    /**
     * Requests that this client move to the location specified by the
     * supplied portal id. A request will be made and when the response is
     * received, the location observers will be notified of success or
     * failure.
     */
    public boolean traversePortal (int portalId, ResultListener rl)
    {
        // look up the destination scene and location
        SpotScene scene = (SpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to traverse portal when we have " +
                        "no scene [portalId=" + portalId + "].");
            return false;
        }

        // find the portal they're talking about
        Portal dest = scene.getPortal(portalId);
        if (dest == null) {
            Log.warning("Requested to traverse non-existent portal " +
                        "[portalId=" + portalId + ", portals=" +
                        StringUtil.toString(scene.getPortals()) + "].");
            return false;
        }

        // prepare to move to this scene (sets up pending data)
        if (!_scdir.prepareMoveTo(dest.targetSceneId, rl)) {
            Log.info("Portal traversal vetoed by scene director " +
                     "[portalId=" + portalId + "].");
            return false;
        }

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        int sceneVer = 0;
        SceneModel pendingModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVer = pendingModel.version;
        }

        // issue a traversePortal request
        Log.info("Issuing traversePortal(" + dest + ", " + sceneVer + ").");
        _sservice.traversePortal(_ctx.getClient(), portalId, sceneVer, _scdir);
        return true;
    }

    /**
     * Issues a request to change our location within the scene to the
     * specified location.
     *
     * @param loc the new location to which to move.
     * @param obs will be notified of success or failure. Most client
     * entities find out about location changes via changes to the
     * occupant info data, but the initiator of a location change request
     * can be notified of its success or failure, primarily so that it can
     * act in anticipation of a successful location change (like by
     * starting a sprite moving toward the new location), but backtrack if
     * it finds out that the location change failed.
     */
    public void changeLocation (Location loc, final ResultListener listener)
    {
        // refuse if there's a pending location change or if we're already
        // at the specified location
        if (loc.equivalent(_location) || (_pendingLoc != null)) {
            Log.info("Not going to " + loc + "; we're at " + _location +
                     " and we're headed to " + _pendingLoc + ".");
            return;
        }

        SpotScene scene = (SpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to change locations, but we're not " +
                        "currently in any scene [loc=" + loc + "].");
            return;
        }

        int sceneId = _scdir.getScene().getId();
        Log.info("Sending changeLocation request [scid=" + sceneId +
                 ", loc=" + loc + "].");

        _pendingLoc = (Location)loc.clone();
        ConfirmListener clist = new ConfirmListener() {
            public void requestProcessed () {
                _location = _pendingLoc;
                _pendingLoc = null;
                if (listener != null) {
                    listener.requestCompleted(_location);
                }
            }

            public void requestFailed (String reason) {
                _pendingLoc = null;
                if (listener != null) {
                    listener.requestFailed(new Exception(reason));
                }
            }
        };
        _sservice.changeLocation(_ctx.getClient(), sceneId, loc, clist);
    }

    /**
     * Issues a request to join the cluster associated with the specified
     * user (starting one if necessary).
     *
     * @param froid the bodyOid of another user; the calling user will
     * be made to join the target user's cluster.
     * @param listener will be notified of success or failure.
     */
    public void joinCluster (int froid, final ResultListener listener)
    {
        SpotScene scene = (SpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to join cluster, but we're not " +
                        "currently in any scene [froid=" + froid + "].");
            return;
        }

        Log.info("Joining cluster [friend=" + froid + "].");

        _sservice.joinCluster(_ctx.getClient(), froid, new ConfirmListener() {
            public void requestProcessed () {
                if (listener != null) {
                    listener.requestCompleted(null);
                }
            }

            public void requestFailed (String reason) {
                if (listener != null) {
                    listener.requestFailed(new Exception(reason));
                }
            }
        });
    }

    /**
     * Sends a chat message to the other users in the cluster to which the
     * location that we currently occupy belongs.
     *
     * @return true if a cluster speak message was delivered, false if we
     * are not in a valid cluster and refused to deliver the request.
     */
    public boolean requestClusterSpeak (String message)
    {
        return requestClusterSpeak(message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Sends a chat message to the other users in the cluster to which the
     * location that we currently occupy belongs.
     *
     * @return true if a cluster speak message was delivered, false if we
     * are not in a valid cluster and refused to deliver the request.
     */
    public boolean requestClusterSpeak (String message, byte mode)
    {
        // make sure we're currently in a scene
        SpotScene scene = (SpotScene)_scdir.getScene();
        if (scene == null) {
            Log.warning("Requested to speak to cluster, but we're not " +
                        "currently in any scene [message=" + message + "].");
            return false;
        }

        // make sure we're part of a cluster
        if (_self.getClusterOid() <= 0) {
            Log.info("Ignoring cluster speak as we're not in a cluster " +
                     "[cloid=" + _self.getClusterOid() + "].");
            return false;
        }

        message = _chatdir.filter(message, null, true);
        if (message != null) {
            _sservice.clusterSpeak(_ctx.getClient(), message, mode);
        }
        return true;
    }

    // documentation inherited from interface
    public void objectAvailable (DObject object)
    {
        clearCluster();
        _clobj = object;
        if (_chatdir != null) {
            _chatdir.addAuxiliarySource(object, CLUSTER_CHAT_TYPE);
        }
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Unable to subscribe to cluster chat object " +
                    "[oid=" + oid + ", cause=" + cause + "].");
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(_self.getClusterField()) &&
            !event.getValue().equals(event.getOldValue())) {
            maybeUpdateCluster();
        }
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // listen to the client object
        client.getClientObject().addListener(this);
        _self = (ClusteredBodyObject)client.getClientObject();

        // we may need to subscribe to a cluster due to session resumption
        maybeUpdateCluster();
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        super.clientObjectDidChange(client);

        // listen to the client object
        client.getClientObject().addListener(this);
        _self = (ClusteredBodyObject)client.getClientObject();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        // clear out our business
        _location = null;
        _pendingLoc = null;
        _sservice = null;
        clearCluster();

        // stop listening to the client object
        client.getClientObject().removeListener(this);
        _self = null;
    }

    // documentation inherited
    protected void fetchServices (Client client)
    {
        _sservice = (SpotService)client.requireService(SpotService.class);
    }

    /**
     * Clean up after a few things when we depart from a scene.
     */
    protected void handleDeparture ()
    {
        // clear out our last known location id
        _location = null;
    }

    /**
     * Checks to see if our cluster has changed and does the necessary
     * subscription machinations if necessary.
     */
    protected void maybeUpdateCluster ()
    {
        int cloid = _self.getClusterOid();
        if ((_clobj == null && cloid <= 0) ||
            (_clobj != null && cloid == _clobj.getOid())) {
            // our cluster didn't change, we can stop now
            return;
        }

        // clear out any old cluster object
        clearCluster();

        // if there's a new cluster object, subscribe to it
        if (_chatdir != null && cloid > 0) {
            DObjectManager omgr = _ctx.getDObjectManager();
            // we'll wire up to the chat director when this completes
            omgr.subscribeToObject(cloid, this);
        }
    }

    /**
     * Convenience routine to unwire chat for and unsubscribe from our
     * current cluster, if any.
     */
    protected void clearCluster ()
    {
        if (_clobj != null) {
            if (_chatdir != null) {
                _chatdir.removeAuxiliarySource(_clobj);
            }
            DObjectManager omgr = _ctx.getDObjectManager();
            omgr.unsubscribeFromObject(_clobj.getOid(), this);
            _clobj = null;
        }
    }

    /** The active client context. */
    protected WhirledContext _ctx;

    /** Access to spot scene services. */
    protected SpotService _sservice;

    /** The scene director with which we are cooperating. */
    protected SceneDirector _scdir;

    /** A casted reference to our clustered body object. */
    protected ClusteredBodyObject _self;

    /** A reference to the chat director with which we coordinate. */
    protected ChatDirector _chatdir;

    /** The location we currently occupy. */
    protected Location _location;

    /** The location to which we have an outstanding change location
     * request. */
    protected Location _pendingLoc;

    /** The cluster chat object for the cluster we currently occupy. */
    protected DObject _clobj;
}
