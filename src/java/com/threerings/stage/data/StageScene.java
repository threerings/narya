//
// $Id: YoScene.java 17013 2004-09-08 02:39:09Z ray $

package com.threerings.stage.data;

import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoUtil;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneImpl;
import com.threerings.whirled.data.SceneUpdate;

import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneImpl;
import com.threerings.whirled.spot.data.SpotSceneModel;

import com.threerings.stage.Log;

/**
 * The implementation of the Stage scene interface.
 */
public class StageScene extends SceneImpl
    implements Scene, SpotScene, Cloneable
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public StageScene (StageSceneModel model, PlaceConfig config)
    {
        super(model, config);
        _model = model;
        _sdelegate = new SpotSceneImpl(SpotSceneModel.getSceneModel(_model));
        readInterestingObjects();
    }

    /**
     * Returns the scene type (e.g. "world", "port", "bank", etc.).
     */
    public String getType ()
    {
        return _model.type;
    }

    /**
     * Returns the zone id to which this scene belongs.
     */
    public int getZoneId ()
    {
        return _model.zoneId;
    }

    /**
     * Sets the type of this scene.
     */
    public void setType (String type)
    {
        _model.type = type;
    }

    /**
     * Get the default color id to use for the specified colorization class,
     * or -1 if no default is set.
     */
    public int getDefaultColor (int classId)
    {
        return _model.getDefaultColor(classId);
    }

    /**
     * Set the default color to use for the specified colorization class id.
     * Setting the colorId to -1 disables the default.
     */
    public void setDefaultColor (int classId, int colorId)
    {
        _model.setDefaultColor(classId, colorId);
    }

    /**
     * Iterates over all of the interesting objects in this scene.
     */
    public Iterator enumerateObjects ()
    {
        return _objects.iterator();
    }

    /**
     * Adds a new object to this scene.
     */
    public void addObject (ObjectInfo info)
    {
        _objects.add(info);

        // add it to the underlying scene model
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            if (!mmodel.addObject(info)) {
                Log.warning("Scene model rejected object add " +
                            "[scene=" + this + ", object=" + info + "].");
            }
        }
    }

    /**
     * Removes an object from this scene.
     */
    public boolean removeObject (ObjectInfo info)
    {
        boolean removed = _objects.remove(info);

        // remove it from the underlying scene model
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            removed = mmodel.removeObject(info) || removed;
        }

        return removed;
    }

    // documentation inherited
    public void updateReceived (SceneUpdate update)
    {
        super.updateReceived(update);

        // update our spot scene delegate
        _sdelegate.updateReceived();

        // re-read our interesting objects
        readInterestingObjects();
    }

    // documentation inherited
    public Object clone ()
        throws CloneNotSupportedException
    {
        // create a new scene with a clone of our model
        return new StageScene((StageSceneModel)_model.clone(), _config);
    }

    // documentation inherited from interface
    public Portal getPortal (int portalId)
    {
        return _sdelegate.getPortal(portalId);
    }

    // documentation inherited from interface
    public int getPortalCount ()
    {
        return _sdelegate.getPortalCount();
    }

    // documentation inherited from interface
    public Iterator getPortals ()
    {
        return _sdelegate.getPortals();
    }

    // documentation inherited from interface
    public Portal getDefaultEntrance ()
    {
        return _sdelegate.getDefaultEntrance();
    }

    // documentation inherited from interface
    public void addPortal (Portal portal)
    {
        _sdelegate.addPortal(portal);
    }

    // documentation inherited from interface
    public void removePortal (Portal portal)
    {
        _sdelegate.removePortal(portal);
    }

    // documentation inherited from interface
    public void setDefaultEntrance (Portal portal)
    {
        _sdelegate.setDefaultEntrance(portal);
    }

    protected void readInterestingObjects ()
    {
        _objects.clear();
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            mmodel.getInterestingObjects(_objects);
        }
    }

    /** A reference to our scene model. */
    protected StageSceneModel _model;

    /** Our spot scene delegate. */
    protected SpotSceneImpl _sdelegate;

    /** A list of all interesting scene objects. */
    protected ArrayList _objects = new ArrayList();
}
