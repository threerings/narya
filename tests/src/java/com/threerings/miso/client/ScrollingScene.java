//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.client;

import java.awt.Rectangle;

import java.util.Iterator;
import java.util.Random;

import com.samskivert.io.PersistenceException;

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
        throws NoSuchTileSetException, PersistenceException
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

    public boolean addObject (ObjectInfo info)
    {
        return true;
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
