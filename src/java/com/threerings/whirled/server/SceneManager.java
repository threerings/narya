//
// $Id: SceneManager.java,v 1.11 2003/02/05 00:23:52 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.server.PlaceManager;
import com.threerings.whirled.data.SceneModel;

/**
 * The scene manager extends the place manager and takes care of basic
 * scene services. Presently that is little more than registering the
 * scene manager with the scene registry so that the manager can be looked
 * up by scene id in addition to place object id.
 */
public class SceneManager extends PlaceManager
{
    /**
     * Returns the runtime scene object (not the scene distributed object)
     * being managed by this scene manager.
     */
    public RuntimeScene getScene ()
    {
        return _scene;
    }

    /**
     * Returns a reference to the scene model from which we created our
     * runtime scene object. This model must reflect any modifications
     * this manager may have made to it in the course of managing the
     * scene as it will be provided to the client as the definitive
     * version of the scene. (The scene manager is responsible for writing
     * changes made to the scene model back to the scene repository.)
     */
    public SceneModel getSceneModel ()
    {
        return _model;
    }

    /**
     * Called by the scene registry once the scene manager has been
     * created (and initialized), but before it is started up.
     */
    protected void setSceneData (
        RuntimeScene scene, SceneModel model, SceneRegistry screg)
    {
        _scene = scene;
        _model = model;
        _screg = screg;

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

        // let the scene registry know that we're up and running
        _screg.sceneManagerDidStart(this);
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

    // documentation inherited
    public String where ()
    {
        return _scene.getName() + " (" + super.where() + ")";
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", scene=").append(_scene);
    }

    /** A reference to our runtime scene implementation which provides a
     * meaningful interpretation of the data in the scene model. */
    protected RuntimeScene _scene;

    /** A reference to the scene model which we keep around because we may
     * have to send it to clients that need updated versions of the model
     * or to update the scene model in the repository if we modify the
     * scene for some reason. */
    protected SceneModel _model;

    /** A reference to the scene registry so that we can call back to it
     * when we're fully initialized. */
    protected SceneRegistry _screg;
}
