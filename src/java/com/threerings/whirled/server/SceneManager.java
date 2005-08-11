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

package com.threerings.whirled.server;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.Invoker;

import com.threerings.crowd.server.PlaceManager;
import com.threerings.presents.server.PresentsServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.server.WhirledServer;
import com.threerings.whirled.util.UpdateList;

/**
 * The scene manager extends the place manager and takes care of basic
 * scene services. Presently that is little more than registering the
 * scene manager with the scene registry so that the manager can be looked
 * up by scene id in addition to place object id.
 */
public class SceneManager extends PlaceManager
{
    /**
     * Returns the scene object (not the scene distributed object) being
     * managed by this scene manager.
     */
    public Scene getScene ()
    {
        return _scene;
    }

    /**
     * Returns {@link UpdateList#getUpdates} for this scene's updates.
     */
    public SceneUpdate[] getUpdates (int fromVersion)
    {
        return _updates.getUpdates(fromVersion);
    }

    /**
     * Called by the scene registry once the scene manager has been
     * created (and initialized), but before it is started up.
     */
    protected void setSceneData (Scene scene, UpdateList updates,
                                 SceneRegistry screg)
    {
        _scene = scene;
        _screg = screg;
        _updates = updates;

        // make sure the list and our version of the scene are in
        // accordance
        if (!_updates.validate(scene.getVersion())) {
            Log.warning("Provided with invalid updates; flushing " +
                        "[where=" + where() +
                        ", sceneId=" + scene.getId() + "].");
            // clear out the update list as it will not allow us to bring
            // clients up to date with our current scene version; instead
            // they'll have to download the whole thing
            _updates = new UpdateList();
        }

        // let derived classes react to the receipt of scene data
        gotSceneData();
    }

    /**
     * A method that can be overridden by derived classes to perform
     * initialization processing after we receive our scene information
     * but before we're started up (and hence registered as an active
     * place).
     */
    protected void gotSceneData ()
    {
    }

    /**
     * We're fully ready to go, so now we register ourselves with the
     * scene registry which will make us available to the clients and
     * system at large.
     */
    protected void didStartup ()
    {
        super.didStartup();

        // Wait until us and all of our subclasses have completely finished
        // running didStartup prior to registering the scene as being ready.
        PresentsServer.omgr.postRunnable(new Runnable() {
            public void run () {
                _screg.sceneManagerDidStart(SceneManager.this);
            }
        });
    }

    /**
     * Called when we have shutdown.
     */
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister ourselves with the scene registry
        _screg.unmapSceneManager(this);
    }

    /**
     * When a modification is made to a scene, the scene manager should
     * create a SceneUpdate instance and pass it to this method which will
     * update the in-memory scene, and apply and record the update in the
     * scene repository.
     *
     * <p> This update will be stored persistently and provided (along
     * with any other accumulated updates) to clients that later request
     * to enter the scene with an old version of the scene data. Updates
     * are not stored forever, but a sizable number of recent updates are
     * stored so that moderately current clients can apply incremental
     * patches to their scenes rather than redownloading entire scenes
     * when they change.
     */
    protected void recordUpdate (final SceneUpdate update)
    {
        // instruct our in-memory copy of the scene to apply the update
        _scene.updateReceived(update);

        // add it to our in memory update list
        _updates.addUpdate(update);

        // and apply and store it in the repository
        WhirledServer.invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                try {
                    _screg.getSceneRepository().applyAndRecordUpdate(
                        _scene.getSceneModel(), update);
                } catch (PersistenceException pe) {
                    Log.warning("Failed to apply scene update " + update + ".");
                    Log.logStackTrace(pe);
                }
                return false;
            }
        });

        // broadcast the update to all occupants of the scene
        _plobj.postMessage(SceneCodes.SCENE_UPDATE, new Object[] { update });
    }

    // documentation inherited
    public String where ()
    {
        return _scene.getName() + " (" + super.where() + ":" +
            _scene.getId() + ")";
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", scene=").append(_scene);
    }

    /** A reference to our scene implementation which provides a
     * meaningful interpretation of the data in the scene model. */
    protected Scene _scene;

    /** A list of the updates tracked for this scene. These will be used
     * to attempt to bring clients up to date efficiently if they request
     * to enter our scene with old scene model data. */
    protected UpdateList _updates;

    /** A reference to the scene registry so that we can call back to it
     * when we're fully initialized. */
    protected SceneRegistry _screg;
}
