//
// $Id: SceneImpl.java,v 1.2 2003/06/11 04:14:11 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.Log;

/**
 * An implementation of the {@link Scene} interface.
 */
public class SceneImpl implements Scene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public SceneImpl (SceneModel model, PlaceConfig config)
    {
        _model = model;
        _config = config;
    }

    /**
     * Instantiates a blank scene implementation. No place config will be
     * associated with this scene.
     */
    public SceneImpl ()
    {
        _model = SceneModel.blankSceneModel();
    }

    // documentation inherited
    public int getId ()
    {
        return _model.sceneId;
    }

    // documentation inherited
    public String getName ()
    {
        return _model.name;
    }

    // documentation inherited
    public int getVersion ()
    {
        return _model.version;
    }

    // documentation inherited
    public PlaceConfig getPlaceConfig ()
    {
        return _config;
    }

    // documentation inherited from interface
    public void setId (int sceneId)
    {
        _model.sceneId = sceneId;
    }

    // documentation inherited from interface
    public void setName (String name)
    {
        _model.name = name;
    }

    // documentation inherited from interface
    public void setVersion (int version)
    {
        _model.version = version;
    }

    // documentation inherited from interface
    public void updateReceived (SceneUpdate update)
    {
        try {
            // validate and apply the update
            update.validate(_model);
            update.apply(_model);
        } catch (Exception e) {
            Log.warning("Error applying update [scene=" + this +
                        ", update=" + update + "].");
            Log.logStackTrace(e);
        }
    }

    // documentation inherited from interface
    public SceneModel getSceneModel ()
    {
        return _model;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[model=" + _model + ", config=" + _config + "]";
    }

    /** A reference to our scene model. */
    protected SceneModel _model;

    /** A reference to our place configuration. */
    protected PlaceConfig _config;
}
