//
// $Id: RuntimeMisoSceneImpl.java,v 1.2 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.server;

import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;

/**
 * A basic implementation of the {@link RuntimeMisoScene} interface which
 * is used by default if no extended implementation is desired.
 */
public class RuntimeMisoSceneImpl
    implements RuntimeMisoScene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model.
     */
    public RuntimeMisoSceneImpl (MisoSceneModel model)
    {
        // stick all of the interesting scene objects into an array list
        for (int ii = 0, ll = model.objectInfo.length; ii < ll; ii++) {
            _objects.add(model.objectInfo[ii]);
        }
    }

    // documentation inherited
    public Iterator enumerateObjects ()
    {
        return _objects.iterator();
    }

    /** The object info records. */
    protected ArrayList _objects = new ArrayList();
}
