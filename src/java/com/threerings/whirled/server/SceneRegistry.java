//
// $Id: SceneRegistry.java,v 1.3 2001/10/02 02:08:16 mdb Exp $

package com.threerings.whirled.server;

import java.util.ArrayList;
import java.util.Properties;

import com.samskivert.util.HashIntMap;
import com.threerings.cocktail.cher.util.Invoker;

import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneObject;
import com.threerings.whirled.server.persist.SceneRepository;

/**
 * The scene registry is responsible for the management of all runtime
 * scenes. It handles interaction with the scene repository and ensures
 * that scenes are loaded into memory when needed and flushed from memory
 * when not needed.
 *
 * <p> The scene repository also takes care of bridging from the blocking,
 * synchronous world of the scene repository to the non-blocking
 * asynchronous world of the distributed object event queue. Thus its
 * interfaces for accessing scenes are structured so as to not block the
 * dobjmgr thread while waiting for scenes to be read from or written to
 * the repository.
 *
 * <p><em>Note:</em> All access to the scene registry should take place
 * from the dobjmgr thread.
 */
public class SceneRegistry
{
    /**
     * Constructs a scene registry, instructing it to load and store
     * scenes using the supplied scene repository.
     */
    public SceneRegistry (SceneRepository screp)
    {
        _screp = screp;

        // we'll use this to do database stuff
        _invoker = new Invoker();
        _invoker.start();
    }

    /**
     * Because scenes must be loaded from the scene repository and this
     * must not be done on the dobjmgr thread, the interface for resolving
     * scenes requires that the entity that wishes for a scene to be
     * resolved implement this callback interface so that it can be
     * notified when a scene has been loaded and initialized.
     */
    public static interface ResolutionListener
    {
        /**
         * Called when the scene has been successfully resolved. The scene
         * manager instance provided can be used to obtain a reference to
         * the scene, or the scene distributed object.
         */
        public void sceneWasResolved (SceneManager scmgr);

        /**
         * Called if some failure occurred in the scene resolution
         * process.
         */
        public void sceneFailedToResolve (int sceneId, Exception reason);
    }

    /**
     * Requests that the specified scene be resolved, which means loaded
     * into the server and initialized if the scene is not currently
     * active. The supplied callback instance will be notified, on the
     * dobjmgr thread, when the scene has been resolved. If the scene is
     * already active, it will be notified immediately (before the call to
     * <code>resolveScene</code> returns).
     *
     * @param sceneId the id of the scene to resolve.
     * @param resolver a reference to a callback instance that will be
     * notified when the scene has been resolved (which may be immediately
     * if the scene is already active).
     */
    public void resolveScene (int sceneId, ResolutionListener target)
    {
        SceneManager mgr = (SceneManager)_scenemgrs.get(sceneId);
        if (mgr != null) {
            // if the scene is already resolved, we're ready to roll
            target.sceneWasResolved(mgr);
            return;
        }

        Log.info("Resolving scene [id=" + sceneId + "].");

        // otherwise we've got to resolve the scene and call them back
        // later; we can manipulate the penders table with impunity here
        // because we only do so on the dobjmgr thread
        ArrayList penders = (ArrayList)_penders.get(sceneId);

        // if we're already in the process of resolving this scene, just
        // add these guys to the list to be notified when it finally is
        // resolved
        if (penders != null) {
            penders.add(target);

        } else {
            // otherwise we've got to initiate the resolution process.
            // first we create the penders list
            penders = new ArrayList();
            _penders.put(sceneId, penders);
            penders.add(target);

            // i don't like cluttering up method declarations with final
            // keywords...
            final int fsceneId = sceneId;

            Log.info("Invoking scene repository [id=" + sceneId + "].");

            // then we queue up an execution unit that'll load the scene
            // and initialize it and all that
            _invoker.postUnit(new Invoker.Unit() {
                // this is run on the invoker thread
                public boolean invoke ()
                {
                    try {
                        _scene = _screp.loadScene(fsceneId);
                    } catch (Exception e) {
                        _cause = e;
                    }
                    return true;
                }

                // this is run on the dobjmgr thread
                public void handleResult ()
                {
                    if (_scene != null) {
                        processSuccessfulResolution(_scene);
                    } else if (_cause != null) {
                        processFailedResolution(fsceneId, _cause);
                    } else {
                        Log.warning("Scene loading unit finished with " +
                                    "neither a scene nor a reason for " +
                                    "failure!?");
                    }
                }

                protected Scene _scene;
                protected Exception _cause;
            });
        }
    }

    protected void processSuccessfulResolution (Scene scene)
    {
        // now that the scene is loaded, we can create a scene manager for
        // it. that will be initialized by the place registry and when
        // that is finally complete, then we can let our penders know
        // what's up

        try {
            SceneManager scmgr = (SceneManager)
                PartyServer.plreg.createPlace(SceneManager.class);

            // configure the scene manager with references to useful
            // stuff; we'll somehow need to convey configuration
            // information for the scene to the scene manager, but for now
            // let's punt
            scmgr.postInit(scene, this);

            // when the scene manager completes its startup procedings, it
            // will call back to the scene registry and let us know that
            // we can turn the penders loose

        } catch (InstantiationException ie) {
            // so close, but no cigar
            processFailedResolution(scene.getId(), ie);
        }
    }

    protected void processFailedResolution (int sceneId, Exception cause)
    {
        // alas things didn't work out, notify our penders
        ArrayList penders = (ArrayList)_penders.remove(sceneId);
        if (penders != null) {
            for (int i = 0; i < penders.size(); i++) {
                ResolutionListener rl = (ResolutionListener)penders.get(i);
                try {
                    rl.sceneFailedToResolve(sceneId, cause);
                } catch (Exception e) {
                    Log.warning("Resolution listener choked.");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    protected void sceneManagerDidInit (SceneManager scmgr)
    {
        // register this scene manager in our table
        int sceneId = scmgr.getScene().getId();
        _scenemgrs.put(sceneId, scmgr);

        Log.info("Registering scene manager [scid=" + sceneId +
                 ", scmgr=" + scmgr + "].");

        // now notify any penders
        ArrayList penders = (ArrayList)_penders.remove(sceneId);
        if (penders != null) {
            for (int i = 0; i < penders.size(); i++) {
                ResolutionListener rl = (ResolutionListener)penders.get(i);
                try {
                    rl.sceneWasResolved(scmgr);
                } catch (Exception e) {
                    Log.warning("Resolution listener choked.");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    protected SceneRepository _screp;
    protected Invoker _invoker;
    
    protected HashIntMap _scenemgrs = new HashIntMap();
    protected HashIntMap _penders = new HashIntMap();
}
