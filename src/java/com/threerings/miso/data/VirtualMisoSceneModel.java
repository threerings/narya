//
// $Id: VirtualMisoSceneModel.java,v 1.2 2003/04/18 18:31:21 mdb Exp $

package com.threerings.miso.data;

import java.awt.Rectangle;

/**
 * A convenient base class for "virtual" scenes which do not allow editing
 * and compute the base and object tiles rather than obtain them from some
 * data structure.
 */
public abstract class VirtualMisoSceneModel extends MisoSceneModel
{
    public VirtualMisoSceneModel ()
    {
    }

    public VirtualMisoSceneModel (int width, int height)
    {
        super(width, height);
    }

    // documentation inherited from interface
    public boolean setBaseTile (int fqTileId, int x, int y)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public void addObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public void updateObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public boolean removeObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }
}
