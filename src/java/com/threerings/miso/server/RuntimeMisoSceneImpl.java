//
// $Id: RuntimeMisoSceneImpl.java,v 1.3 2003/02/04 17:24:56 mdb Exp $

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

    // documentation inherited from interface
    public void addObject (ObjectInfo info)
    {
        // slap it on our list
        _objects.add(info);

        // slip it into the interesting array
        int ocount = _model.objectInfo.length;
        ObjectInfo[] objectInfo = new ObjectInfo[ocount+1];
        System.arraycopy(_model.objectInfo, 0, objectInfo, 0, ocount);
        objectInfo[ocount] = info;
        _model.objectInfo = objectInfo;
    }

    /** Our scene model. */
    protected MisoSceneModel _model;

    /** The object info records. */
    protected ArrayList _objects = new ArrayList();
}
