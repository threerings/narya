//
// $Id: RuntimeMisoSceneImpl.java,v 1.1 2002/12/11 23:07:21 shaper Exp $

package com.threerings.miso.server;

import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.miso.scene.MisoSceneModel;
import com.threerings.miso.scene.SceneObject;

/**
 * A basic implementation of the {@link RuntimeMisoScene} interface which
 * is used by default if no extended implementation is desired.
 */
public class RuntimeMisoSceneImpl
    implements RuntimeMisoScene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public RuntimeMisoSceneImpl (MisoSceneModel model)
    {
        // keep a casted reference to our scene model around
        _model = model;

        // build a list of all objects in the scene
        populateSceneObjects();
    }

    /**
     * Gathers and stores information about all objects in the scene.
     */
    protected void populateSceneObjects ()
    {
        int ocount = _model.objectTileIds.length;
        for (int ii = 0; ii < ocount; ii += 3) {
            RuntimeSceneObject scobj = new RuntimeSceneObject();
            scobj.x = _model.objectTileIds[ii];
            scobj.y = _model.objectTileIds[ii+1];
            scobj.action = _model.objectActions[ii/3];
            _objects.add(scobj);
        }
    }

    // documentation inherited
    public Iterator enumerateSceneObjects ()
    {
        return _objects.iterator();
    }

    /** A casted reference to our scene model. */
    protected MisoSceneModel _model;

    /** The scene object records. */
    protected ArrayList _objects = new ArrayList();
}
