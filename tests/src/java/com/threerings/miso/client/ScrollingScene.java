//
// $Id: ScrollingScene.java,v 1.7 2002/04/27 23:07:58 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Point;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import com.samskivert.io.PersistenceException;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetRepository;

import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.util.MisoContext;

/**
 * Provides an infinite array of tiles in which to scroll.
 */
public class ScrollingScene implements DisplayMisoScene
{
    public ScrollingScene (MisoContext ctx)
        throws NoSuchTileSetException, NoSuchTileException, PersistenceException
    {
        // locate the water tileset
        TileSetRepository tsrepo = ctx.getTileManager().getTileSetRepository();
        Iterator iter = tsrepo.enumerateTileSets();
        TileSet wtset = null;
        while (iter.hasNext()) {
            TileSet tset = (TileSet)iter.next();
            // yay for built-in regex support!
            if (tset.getName().matches(".*[Ww]ater.*") &&
                tset instanceof BaseTileSet) {
                wtset = tset;
                break;
            }
        }

        if (wtset == null) {
            throw new RuntimeException("Unable to locate water tileset.");
        }

        // grab our four repeating tiles
        _tiles = new BaseTile[wtset.getTileCount()];
        for (int ii = 0; ii < wtset.getTileCount(); ii++) {
            _tiles[ii] = (BaseTile)wtset.getTile(ii);
        }
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        long seed = ((x^y) ^ multiplier) & mask;
        long hash = (seed * multiplier + addend) & mask;
        int tidx = (int)((hash >> 10) % _tiles.length);
        return _tiles[tidx];
    }

    // documentation inherited from interface
    public Tile getFringeTile (int x, int y)
    {
        return null;
    }

    // documentation inherited from interface
    public Iterator getObjectTiles ()
    {
        return Collections.EMPTY_LIST.iterator();
    }

    // documentation inherited from interface
    public Point getObjectCoords (ObjectTile tile)
    {
        return null;
    }

    // documentation inherited from interface
    public String getObjectAction (ObjectTile tile)
    {
        return null;
    }

    protected BaseTile[] _tiles;
    protected Random _rand = new Random();

    protected final static long multiplier = 0x5DEECE66DL;
    protected final static long addend = 0xBL;
    protected final static long mask = (1L << 48) - 1;

    protected static final int WATER_TILESET_ID = 8;
}
