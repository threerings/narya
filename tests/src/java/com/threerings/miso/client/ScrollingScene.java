//
// $Id: ScrollingScene.java,v 1.4 2002/04/15 17:47:05 mdb Exp $

package com.threerings.miso.scene;

import java.util.Random;

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
        _tiles[0] = (BaseTile)ctx.getTileManager().getTile(5, 0);
        _tiles[1] = (BaseTile)ctx.getTileManager().getTile(5, 1);
        _tiles[2] = (BaseTile)ctx.getTileManager().getTile(5, 2);
        _tiles[3] = (BaseTile)ctx.getTileManager().getTile(5, 3);
        _tiles[4] = (BaseTile)ctx.getTileManager().getTile(5, 4);
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        long seed = ((x^y) ^ multiplier) & mask;
        long hash = (seed * multiplier + addend) & mask;
        int tidx = (int)((hash >> 10) % 5);
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

    protected BaseTile[] _tiles = new BaseTile[5];
    protected Random _rand = new Random();

    protected final static long multiplier = 0x5DEECE66DL;
    protected final static long addend = 0xBL;
    protected final static long mask = (1L << 48) - 1;
}
