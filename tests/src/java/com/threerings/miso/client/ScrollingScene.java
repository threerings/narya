//
// $Id: ScrollingScene.java,v 1.14 2004/02/25 14:51:26 mdb Exp $

package com.threerings.miso.client;

import java.awt.Rectangle;

import java.util.Iterator;
import java.util.Random;

import com.samskivert.io.PersistenceException;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetRepository;

import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.util.ObjectSet;
import com.threerings.miso.data.ObjectInfo;

/**
 * Provides an infinite array of tiles in which to scroll.
 */
public class ScrollingScene extends MisoSceneModel
{
    public ScrollingScene (MisoContext ctx)
        throws NoSuchTileSetException, NoSuchTileException, PersistenceException
    {
        // locate the water tileset
        TileSetRepository tsrepo = ctx.getTileManager().getTileSetRepository();
        Iterator iter = tsrepo.enumerateTileSets();
        String tsname = null;
        while (iter.hasNext()) {
            TileSet tset = (TileSet)iter.next();
            // yay for built-in regex support!
            if (tset.getName().matches(".*[Ww]ater.*") &&
                tset instanceof BaseTileSet) {
                tsname = tset.getName();
                break;
            }
        }

        if (tsname == null) {
            throw new RuntimeException("Unable to locate water tileset.");
        }

        // now we look the tileset up by name so that it is properly
        // initialized and all that business
        TileSet wtset = ctx.getTileManager().getTileSet(tsname);

        // grab our four repeating tiles
        _tiles = new BaseTile[wtset.getTileCount()];
        for (int ii = 0; ii < wtset.getTileCount(); ii++) {
            _tiles[ii] = (BaseTile)wtset.getTile(ii);
        }
    }

    public int getBaseTileId (int x, int y)
    {
        return -1;
    }

    public boolean setBaseTile (int fqTileId, int x, int y)
    {
        return false;
    }

    public void addObject (ObjectInfo info)
    {
    }

    public void getObjects (Rectangle region, ObjectSet set)
    {
    }

    public void updateObject (ObjectInfo info)
    {
    }

    public boolean removeObject (ObjectInfo info)
    {
        return false;
    }

    // documentation inherited from interface
    public BaseTile getBaseTile (int x, int y)
    {
        long seed = ((x^y) ^ multiplier) & mask;
        long hash = (seed * multiplier + addend) & mask;
        int tidx = (int)((hash >> 10) % _tiles.length);
        return _tiles[tidx];
    }

    protected BaseTile[] _tiles;
    protected Random _rand = new Random();

    protected final static long multiplier = 0x5DEECE66DL;
    protected final static long addend = 0xBL;
    protected final static long mask = (1L << 48) - 1;

    protected static final int WATER_TILESET_ID = 8;
}
