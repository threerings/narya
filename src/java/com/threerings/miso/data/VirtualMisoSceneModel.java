//
// $Id: VirtualMisoSceneModel.java,v 1.5 2004/08/13 20:48:27 mdb Exp $

package com.threerings.miso.data;

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

    // documentation inherited from interface
    public boolean setBaseTile (int fqTileId, int x, int y)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public boolean addObject (ObjectInfo info)
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
