//
// $Id: SceneDirector.java,v 1.11 2002/04/15 16:28:03 shaper Exp $

package com.threerings.whirled.client;

import java.io.IOException;
import com.samskivert.util.HashIntMap;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.WhirledContext;

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
public class SceneDirector
    implements SceneCodes, LocationDirector.FailureHandler
{
    /**
     * Creates a new scene director with the specified context.
     *
     * @param ctx the active client context.
     * @param locdir the location director in use on the client, with
     * which the scene director will coordinate when changing location.
     * @param screp the entity from which the scene director will load
     * scene data from the local client scene storage.
     * @param dsfact the factory that knows which derivation of {@link
     * DisplayScene} to create for the current system.
     */
    public SceneDirector (WhirledContext ctx, LocationDirector locdir,
                          SceneRepository screp, DisplaySceneFactory dsfact)
    {
        // we'll need these for later
        _ctx = ctx;
        _locdir = locdir;
        _screp = screp;
        _dsfact = dsfact;

        // set ourselves up as a failure handler with the location
        // director because we need to do special processing
        _locdir.setFailureHandler(this);
    }

    /**
     * Returns the dispaly scene object associated with the scene we
     * currently occupy or null if we currently occupy no scene.
     */
    public DisplayScene getScene ()
    {
        return _scene;
    }

    /**
     * Requests that this client move the specified scene. A request will
     * be made and when the response is received, the location observers
     * will be notified of success or failure.
     */
    public void moveTo (int sceneId)
    {
        // prepare to move to this scene (sets up pending data)
        if (!prepareMoveTo(sceneId)) {
            return;
        }

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        int sceneVers = 0;
        if (_pendingModel != null) {
            sceneVers = _pendingModel.version;
        }

        // issue a moveTo request
        SceneService.moveTo(_ctx.getClient(), sceneId, sceneVers, this);
    }

    /**
     * Prepares to move to the requested scene. The location observers are
     * asked to ratify the move and our pending scene mode is loaded from
     * the scene repository. This can be called by cooperating directors
     * that need to coopt the moveTo process.
     */
    public boolean prepareMoveTo (int sceneId)
    {
        // first check to see if our observers are happy with this move
        // request
        if (!_locdir.mayMoveTo(sceneId)) {
            return false;
        }

        // complain if we're over-writing a pending request
        if (_pendingSceneId != -1) {
            Log.warning("We appear to have a moveTo request outstanding " +
                        "[psid=" + _pendingSceneId +
                        ", nsid=" + sceneId + "].");
            // but we're going to fall through and do it anyway because
            // refusing to switch scenes at this point will inevitably
            // result in some strange bug causing a move request to be
            // dropped by the server and the client that did it to be
            // totally hosed because they can no longer move to new scenes
            // because they still have an outstanding request
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

    /**
     * Called in response to a successful <code>moveTo</code> request.
     */
    public void handleMoveSucceeded (
        int invid, int placeId, PlaceConfig config)
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
        }

        // and finally create a display scene instance with the model and
        // the place config
        _scene = _dsfact.createScene(_model, config);
    }

    /**
     * Called in response to a successful <code>moveTo</code> request when
     * our cached scene was out of date and the server determined that we
     * needed an updated copy.
     */
    public void handleMoveSucceededPlusUpdate (
        int invid, int placeId, PlaceConfig config, SceneModel model)
    {
        // update the model in the repository
        try {
            _screp.updateSceneModel(model);
        } catch (IOException ioe) {
            Log.warning("Danger Will Robinson! We were unable to update " +
                        "our scene cache with a new version of a scene " +
                        "provided by the server " +
                        "[newVersion=" + model.version + "].");
            Log.logStackTrace(ioe);
        }

        // update our scene cache
        _scache.put(model.sceneId, model);

        // and pass through to the normal move succeeded handler
        handleMoveSucceeded(invid, placeId, config);
    }

    /**
     * Called in response to a failed <code>moveTo</code> request.
     */
    public void handleMoveFailed (int invid, String reason)
    {
        // clear out our pending request oid
        int sceneId = _pendingSceneId;
        _pendingSceneId = -1;

        // let our observers know that something has gone horribly awry
        _locdir.failedToMoveTo(sceneId, reason);
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
        SceneModel model = (SceneModel)_scache.get(sceneId);

        // load from the repository if it's not cached
        if (model == null) {
            try {
                model = _screp.loadSceneModel(sceneId);
                _scache.put(sceneId, model);

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

    /** Access to general client services. */
    protected WhirledContext _ctx;

    /** The client's active location director. */
    protected LocationDirector _locdir;

    /** The entity via which we load scene data. */
    protected SceneRepository _screp;

    /** The entity we use to create display scenes from scene models. */
    protected DisplaySceneFactory _dsfact;

    /** A cache of scene model information. */
    protected HashIntMap _scache = new HashIntMap();

    /** The display scene object for the scene we currently occupy. */
    protected DisplayScene _scene;

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
