//
// $Id: ScrollingScene.java,v 1.1 2002/02/17 23:48:38 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;

import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.util.MisoContext;

/**
 * Provides an infinite array of tiles in which to scroll.
 */
public class ScrollingScene implements DisplayMisoScene
{
    public ScrollingScene (MisoContext ctx)
        throws NoSuchTileSetException, NoSuchTileException
    {
        // grab our four repeating tiles
        _tiles[0] = (BaseTile)ctx.getTileManager().getTile(11, 0);
        _tiles[1] = (BaseTile)ctx.getTileManager().getTile(12, 0);
        _tiles[2] = (BaseTile)ctx.getTileManager().getTile(13, 0);
        _tiles[3] = (BaseTile)ctx.getTileManager().getTile(14, 0);
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        int tidx = 2 * (Math.abs(x) % 2) + Math.abs(y) % 2;
        return _tiles[tidx];
    }

    // documentation inherited from interface
    public Tile getFringeTile (int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public ObjectTile getObjectTile (int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public String getObjectAction (int column, int row)
    {
        return null;
    }

    protected BaseTile[] _tiles = new BaseTile[4];
}
