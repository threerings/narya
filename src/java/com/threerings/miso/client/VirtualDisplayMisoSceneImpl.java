//
// $Id: VirtualDisplayMisoSceneImpl.java,v 1.1 2003/02/12 07:21:15 mdb Exp $

package com.threerings.miso.client;

import java.awt.Rectangle;

import com.threerings.media.tile.Tile;

import com.threerings.miso.client.util.ObjectSet;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.tile.BaseTile;

/**
 * Provides a useful base class for "virtual" {@link DisplayMisoScene}
 * implementations. These return tiles based on some algorithm rather than
 * a repository of predefined tile data.
 */
public class VirtualDisplayMisoSceneImpl
    implements DisplayMisoScene
{
    // documentation inherited from interface
    public int getBaseTileId (int x, int y)
    {
        return -1;
    }

    // documentation inherited from interface
    public void getObjects (Rectangle region, ObjectSet set)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void setBaseTile (int fqTileId, int x, int y)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void setBaseTiles (Rectangle r, int setId, int setSize)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public ObjectInfo addObject (int fqTileId, int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public boolean removeObject (ObjectInfo info)
    {
        return false;
    }

    // documentation inherited from interface
    public MisoSceneModel getSceneModel ()
    {
        return null;
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public Tile getFringeTile (int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public boolean canTraverse (Object traverser, int x, int y)
    {
        return true;
    }
}
