//
// $Id$
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

package com.threerings.whirled.client;

import java.io.IOException;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.SceneFactory;
import com.threerings.whirled.util.WhirledContext;
import com.threerings.whirled.data.SceneUpdate;

/**
 * The scene director is the client's interface to all things scene
 * related. It interfaces with the scene repository to ensure that scene
 * objects are available when the client enters a particular scene. It
 * handles moving from scene to scene (it coordinates with the {@link
 * LocationDirector} in order to do this).
 *
 * <p> Note that when the scene director is in use instead of the location
 * director, scene ids instead of place oids will be supplied to {@link
 * LocationObserver#locationMayChange} and {@link
 * LocationObserver#locationChangeFailed}.
 */
public class SceneDirector extends BasicDirector
    implements SceneCodes, LocationDirector.FailureHandler,
               SceneReceiver, SceneService.SceneMoveListener
{
    /**
     * Creates a new scene director with the specified context.
     *
     * @param ctx the active client context.
     * @param locdir the location director in use on the client, with
     * which the scene director will coordinate when changing location.
     * @param screp the entity from which the scene director will load
     * scene data from the local client scene storage. This may be null
     * when the SceneDirector is constructed, but it should be
     * supplied via {@link #setSceneRepository} prior to really using
     * this director.
     * @param fact the factory that knows which derivation of {@link
     * Scene} to create for the current system.
     */
    public SceneDirector (WhirledContext ctx, LocationDirector locdir,
                          SceneRepository screp, SceneFactory fact)
    {
        super(ctx);

        // we'll need these for later
        _ctx = ctx;
        _locdir = locdir;
        setSceneRepository(screp);
        _fact = fact;

        // set ourselves up as a failure handler with the location
        // director because we need to do special processing
        _locdir.setFailureHandler(this);

        // register for scene notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(
            new SceneDecoder(this));
    }

    /**
     * Set the scene repository.
     */
    public void setSceneRepository (SceneRepository screp)
    {
        _screp = screp;
        _scache.clear();
    }

    /**
     * Returns the display scene object associated with the scene we
     * currently occupy or null if we currently occupy no scene.
     */
    public Scene getScene ()
    {
        return _scene;
    }

    /**
     * Requests that this client move the specified scene. A request will
     * be made and when the response is received, the location observers
     * will be notified of success or failure.
     *
     * @return true if the move to request was issued, false if it was
     * rejected by a location observer or because we have another request
     * outstanding.
     */
    public boolean moveTo (int sceneId)
    {
        // make sure the sceneId is valid
        if (sceneId < 0) {
            Log.warning("Refusing moveTo(): invalid sceneId " + sceneId + ".");
            return false;
        }

        // sanity-check the destination scene id
        if (sceneId == _sceneId) {
            Log.warning("Refusing request to move to the same scene " +
                        "[sceneId=" + sceneId + "].");
            return false;
        }

        // prepare to move to this scene (sets up pending data)
        if (!prepareMoveTo(sceneId, null)) {
            return false;
        }

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        int sceneVers = 0;
        if (_pendingModel != null) {
            sceneVers = _pendingModel.version;
        }

        // issue a moveTo request
        Log.info("Issuing moveTo(" + sceneId + ", " + sceneVers + ").");
        _sservice.moveTo(_ctx.getClient(), sceneId, sceneVers, this);
        return true;
    }

    /**
     * Prepares to move to the requested scene. The location observers are
     * asked to ratify the move and our pending scene mode is loaded from
     * the scene repository. This can be called by cooperating directors
     * that need to coopt the moveTo process.
     */
    public boolean prepareMoveTo (int sceneId, ResultListener rl)
    {
        // first check to see if our observers are happy with this move
        // request
        if (!_locdir.mayMoveTo(sceneId, rl)) {
            return false;
        }

        // we need to call this both to mark that we're issuing a move
        // request and to check to see if the last issued request should
        // be considered stale
        boolean refuse = _locdir.checkRepeatMove();

        // complain if we're over-writing a pending request
        if (_pendingSceneId != -1) {
            if (refuse) {
                Log.warning("Refusing moveTo; We have a request outstanding " +
                            "[psid=" + _pendingSceneId +
                            ", nsid=" + sceneId + "].");
                return false;

            } else {
                Log.warning("Overriding stale moveTo request " +
                            "[psid=" + _pendingSceneId +
                            ", nsid=" + sceneId + "].");
            }
        }

        // load up the pending scene so that we can communicate it's most
        // recent version to the server
        _pendingModel = loadSceneModel(sceneId);

        // make a note of our pending scene id
        _pendingSceneId = sceneId;

        // all systems go
        return true;
    }

    /**
     * Returns the model loaded in preparation for a scene
     * transition. This is made available only for cooperating directors
     * which may need to coopt the scene transition process. The pending
     * model is only valid immediately following a call to {@link
     * #prepareMoveTo}.
     */
    public SceneModel getPendingModel ()
    {
        return _pendingModel;
    }

    // documentation inherited from interface
    public void moveSucceeded (int placeId, PlaceConfig config)
    {
        // our move request was successful, deal with subscribing to our
        // new place object
        _locdir.didMoveTo(placeId, config);

        // since we're committed to moving to the new scene, we'll
        // parallelize and go ahead and load up the new scene now rather
        // than wait until subscription to our place object succeeds

        // keep track of our previous scene info
        _previousSceneId = _sceneId;

        // clear out the old info
        clearScene();

        // make the pending scene the active scene
        _sceneId = _pendingSceneId;
        _pendingSceneId = -1;

        // load the new scene model
        _model = loadSceneModel(_sceneId);

        // complain if we didn't find a scene
        if (_model == null) {
            Log.warning("Aiya! Unable to load scene [sid=" + _sceneId +
                        ", plid=" + placeId + "].");
            return;
        }

        // and finally create a display scene instance with the model and
        // the place config
        _scene = _fact.createScene(_model, config);
    }

    // documentation inherited from interface
    public void moveSucceededWithUpdates (
        int placeId, PlaceConfig config, SceneUpdate[] updates)
    {
        Log.info("Got updates [placeId=" + placeId + ", config=" + config +
                 ", updates=" + StringUtil.toString(updates) + "].");

        // apply the updates to our cached scene
        SceneModel model = loadSceneModel(_pendingSceneId);
        boolean failure = false;
        for (int ii = 0; ii < updates.length; ii++) {
            try {
                updates[ii].validate(model);
            } catch (IllegalStateException ise) {
                Log.warning("Scene update failed validation [model=" + model +
                            ", update=" + updates[ii] +
                            ", error=" + ise.getMessage() + "].");
                failure = true;
                break;
            }

            try {
                updates[ii].apply(model);
            } catch (Exception e) {
                Log.warning("Failure applying scene update [model=" + model +
                            ", update=" + updates[ii] + "].");
                Log.logStackTrace(e);
                failure = true;
                break;
            }
        }

        if (failure) {
            // delete the now half-booched scene model from the repository
            try {
                _screp.deleteSceneModel(_pendingSceneId);
            } catch (IOException ioe) {
                Log.warning("Failure removing booched scene model " +
                            "[sceneId=" + _pendingSceneId + "].");
                Log.logStackTrace(ioe);
            }

            // act as if the scene move failed, though we'll be in a funny
            // state because the server thinks we've changed scenes, but
            // the client can try again without its booched scene model
            requestFailed(INTERNAL_ERROR);
            return;
        }

        // store the updated scene in the repository
        try {
            _screp.storeSceneModel(model);
        } catch (IOException ioe) {
            Log.warning("Failed to update repository with updated scene " +
                        "[sceneId=" + model.sceneId + "].");
            Log.logStackTrace(ioe);
        }

        // finally pass through to the normal success handler
        moveSucceeded(placeId, config);
    }

    // documentation inherited from interface
    public void moveSucceededWithScene (
        int placeId, PlaceConfig config, SceneModel model)
    {
        Log.info("Got updated scene model [placeId=" + placeId +
                 ", config=" + config + ", scene=" + model.sceneId + "/" +
                 model.name + "/" + model.version + "].");

        // update the model in the repository
        try {
            _screp.storeSceneModel(model);
        } catch (IOException ioe) {
            Log.warning("Failed to update repository with new version " +
                        "[sceneId=" + model.sceneId +
                        ", nvers=" + model.version + "].");
            Log.logStackTrace(ioe);
        }

        // update our scene cache
        _scache.put(new Integer(model.sceneId), model);

        // and pass through to the normal move succeeded handler
        moveSucceeded(placeId, config);
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        // clear out our pending request oid
        int sceneId = _pendingSceneId;
        _pendingSceneId = -1;

        // let our observers know that something has gone horribly awry
        _locdir.failedToMoveTo(sceneId, reason);
    }

    /**
     * Called to clean up our place and scene state information when we
     * leave a scene.
     */
    public void didLeaveScene ()
    {
        // let the location director know what's up
        _locdir.didLeavePlace();

        // clear out our own scene state
        clearScene();
    }

    // documentation inherited from interface
    public void forcedMove (int sceneId)
    {
        Log.info("Moving at request of server [sceneId=" + sceneId + "].");

        // clear out our old scene and place data
        didLeaveScene();

        // move to the new scene
        moveTo(sceneId);
    }

    /**
     * Called when something breaks down in the process of performing a
     * <code>moveTo</code> request.
     */
    public void recoverFailedMove (int placeId)
    {
        // we'll need this momentarily
        int sceneId = _sceneId;

        // clear out our now bogus scene tracking info
        clearScene();

        // if we were previously somewhere (and that somewhere isn't where
        // we just tried to go), try going back to that happy place
        if (_previousSceneId != -1 && _previousSceneId != sceneId) {
            moveTo(_previousSceneId);
        }
    }

    /**
     * Clears out our current scene information and releases the scene
     * model for the loaded scene back to the cache.
     */
    protected void clearScene ()
    {
        // clear out our scene id info
        _sceneId = -1;

        // release the old scene model
        releaseSceneModel(_model);

        // clear out our references
        _model = null;
        _scene = null;
    }

    /**
     * Loads a scene from the repository. If the scene is cached, it will
     * be returned from the cache instead.
     */
    protected SceneModel loadSceneModel (int sceneId)
    {
        // first look in the model cache
        Integer key = new Integer(sceneId);
        SceneModel model = (SceneModel)_scache.get(key);

        // load from the repository if it's not cached
        if (model == null) {
            try {
                model = _screp.loadSceneModel(sceneId);
                _scache.put(key, model);

            } catch (NoSuchSceneException nsse) {
                // nothing special here, just fall through and return null

            } catch (IOException ioe) {
                // complain first, then return null
                Log.warning("Error loading scene [scid=" + sceneId +
                            ", error=" + ioe + "].");
            }
        }

        return model;
    }

    /**
     * Unloads a scene model that was previously loaded via {@link
     * #loadSceneModel}. The model will probably continue to live in the
     * cache for a while in case we quickly return to it.
     */
    protected void releaseSceneModel (SceneModel model)
    {
        // we're cool if we're called with null
        if (model == null) {
            return;
        }
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        // clear out our business
        clearScene();
        _scache.clear();
        _pendingSceneId = -1;
        releaseSceneModel(_pendingModel);
        _previousSceneId = -1;
        _sservice = null;
    }

    // documentation inherited from interface
    protected void fetchServices (Client client)
    {
        // get a handle on our scene service
        _sservice = (SceneService)client.requireService(SceneService.class);
    }

    /** Access to general client services. */
    protected WhirledContext _ctx;

    /** Access to our scene services. */
    protected SceneService _sservice;

    /** The client's active location director. */
    protected LocationDirector _locdir;

    /** The entity via which we load scene data. */
    protected SceneRepository _screp;

    /** The entity we use to create scenes from scene models. */
    protected SceneFactory _fact;

    /** A cache of scene model information. */
    protected LRUHashMap _scache = new LRUHashMap(5);

    /** The display scene object for the scene we currently occupy. */
    protected Scene _scene;

    /** The scene model for the scene we currently occupy. */
    protected SceneModel _model;

    /** The id of the scene we currently occupy. */
    protected int _sceneId = -1;

    /** Our most recent copy of the scene model for the scene we're about
     * to enter. */
    protected SceneModel _pendingModel;

    /** The id of the scene for which we have an outstanding moveTo
     * request, or -1 if we have no outstanding request. */
    protected int _pendingSceneId = -1;

    /** The id of the scene we previously occupied. */
    protected int _previousSceneId = -1;
}
