//
// $Id: SceneDirector.java,v 1.3 2001/10/01 22:16:02 mdb Exp $

package com.threerings.whirled.client;

import java.io.IOException;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.dobj.ObjectAccessException;
import com.threerings.cocktail.cher.util.IntMap;

import com.threerings.cocktail.party.client.LocationDirector;
import com.threerings.cocktail.party.client.LocationObserver;
import com.threerings.cocktail.party.data.PlaceObject;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.util.WhirledContext;

/**
 * The scene director is the client's interface to all things scene
 * related. It interfaces with the scene repository to ensure that scene
 * objects are available when the client enters a particular scene. It
 * handles moving from scene to scene (it extends and replaces the
 * location director in order to do this).
 *
 * <p> Note that when the scene director is in use instead of the location
 * director, scene ids instead of place oids will be supplied to {@link
 * com.threerings.cocktail.party.client.LocationObserver#locationMayChange}
 * and {@link
 * com.threerings.cocktail.party.client.LocationObserver#locationChangeFailed}.
 */
public class SceneDirector
    extends LocationDirector implements SceneCodes
{
    /**
     * Creates a new scene director with the specified context.
     */
    public SceneDirector (WhirledContext ctx, SceneRepository screp)
    {
        super(ctx);

        // we'll need these for later
        _ctx = ctx;
        _screp = screp;
    }

    /**
     * Returns the scene object associated with the scene we currently
     * occupy or null if we currently occupy no scene.
     */
    public Scene getScene ()
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
        // first check to see if our observers are happy with this move
        // request
        if (!mayMoveTo(sceneId)) {
            return;
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
        int sceneVers = 0;
        _pendingScene = loadScene(sceneId);
        // if we were unable to load it, assume a previous version of zero
        if (_pendingScene != null) {
            sceneVers = _pendingScene.getVersion();
        }

        // make a note of our pending scene id
        _pendingSceneId = sceneId;

        // issue a moveTo request
        SceneService.moveTo(_ctx.getClient(), sceneId, sceneVers, this);
    }

    /**
     * Called in response to a successful <code>moveTo</code> request.
     */
    public void handleMoveSucceeded (int invid, int placeId)
    {
        // our move request was successful, deal with subscribing to our
        // new place object
        didMoveTo(placeId);

        // since we're committed to moving to the new scene, we'll
        // parallelize and go ahead and load up the new scene now rather
        // than wait until subscription to our place object succeeds

        // release the old scene
        releaseScene(_scene);

        // update our scene id tracking fields
        _previousSceneId = _sceneId;
        _sceneId = _pendingSceneId;
        _pendingSceneId = -1;

        // and load the new scene
        _scene = loadScene(_sceneId);

        // complain if we didn't find a scene
        if (_scene == null) {
            Log.warning("Aiya! Unable to load scene [sid=" + _sceneId +
                        ", plid=" + placeId + "].");
        }
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
        notifyFailure(sceneId, reason);
    }

    protected void recoverFailedMove (int placeId)
    {
        // clear out our now bogus scene tracking info
        int sceneId = _sceneId;
        _sceneId = -1;
        releaseScene(_scene);
        _scene = null;

        // if we were previously somewhere (and that somewhere isn't where
        // we just tried to go), try going back to that happy place
        if (_previousSceneId != -1 && _previousSceneId != sceneId) {
            moveTo(_previousSceneId);
        }
    }

    /**
     * Loads a scene from the repository. If the scene is cached, it will
     * be returned from the cache instead.
     */
    protected Scene loadScene (int sceneId)
    {
        // first look in the cache
        Scene scene = (Scene)_scache.get(sceneId);

        // load from the repository if it's not cached
        if (scene == null) {
            try {
                scene = _screp.loadScene(sceneId);
                _scache.put(sceneId, scene);

            } catch (NoSuchSceneException nsse) {
                // nothing special here, just fall through and return null

            } catch (IOException ioe) {
                // complain first, then return null
                Log.warning("Error loading scene [scid=" + sceneId +
                            ", error=" + ioe + "].");
            }
        }

        return scene;
    }

    /**
     * Unloads a scene that was previously loaded via {@link #loadScene}.
     * The scene will probably continue to live in the cache for a while
     * in case we quickly return to it.
     */
    protected void releaseScene (Scene scene)
    {
        // we're cool if we're called with null
        if (scene == null) {
            return;
        }
    }

    protected WhirledContext _ctx;
    protected SceneRepository _screp;
    protected IntMap _scache = new IntMap();

    /** The scene object of the scene we currently occupy. */
    protected Scene _scene;

    /** The id of the scene we currently occupy. */
    protected int _sceneId = -1;

    /** Our most recent copy of the scene we're about to enter. */
    protected Scene _pendingScene;

    /**
     * The id of the scene for which we have an outstanding moveTo
     * request, or -1 if we have no outstanding request.
     */
    protected int _pendingSceneId = -1;

    /** The id of the scene we previously occupied. */
    protected int _previousSceneId = -1;
}
