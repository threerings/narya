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

package com.threerings.miso.tile;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.CheapIntMap;
import com.samskivert.util.QuickSort;

import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.ImageUtil;

import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.Log;
import com.threerings.miso.data.MisoSceneModel;

/**
 * Automatically fringes a scene according to the rules in the supplied
 * fringe configuration.
 */
public class AutoFringer
{
    /**
     * Constructs an instance that will fringe according to the rules in
     * the supplied fringe configuration.
     */
    public AutoFringer (FringeConfiguration fringeconf, ImageManager imgr,
                        TileManager tmgr)
    {
        _fringeconf = fringeconf;
        _imgr = imgr;
        _tmgr = tmgr;
    }

    /**
     * Compute and return the fringe tile to be inserted at the specified
     * location.
     */
    public BaseTile getFringeTile (MisoSceneModel scene, int col, int row,
                                   HashMap masks)
    {
        // get the tileset id of the base tile we are considering
        int underset = scene.getBaseTileId(col, row) >> 16;

        // start with a clean temporary fringer map
        _fringers.clear();
        boolean passable = true;

        // walk through our influence tiles
        for (int y = row - 1, maxy = row + 2; y < maxy; y++) {
            for (int x = col - 1, maxx = col + 2; x < maxx; x++) {
                // we sensibly do not consider ourselves
                if ((x == col) && (y == row)) {
                    continue;
                }

                // determine the tileset for this tile
                int btid = scene.getBaseTileId(x, y);
                int baseset = (btid <= 0) ?
                    scene.getDefaultBaseTileSet() : (btid >> 16);

                // determine if it fringes on our tile
                int pri = _fringeconf.fringesOn(baseset, underset);
                if (pri == -1) {
                    continue;
                }

                FringerRec fringer = (FringerRec)_fringers.get(baseset);
                if (fringer == null) {
                    fringer = new FringerRec(baseset, pri);
                    _fringers.put(baseset, fringer);
                }

                // now turn on the appropriate fringebits
                fringer.bits |= FLAGMATRIX[y - row + 1][x - col + 1];

                // See if a tile that fringes on us kills our passability,
                // but don't count the default base tile against us, as
                // we allow users to splash in the water.
                if (passable && (btid > 0)) {
                    try {
                        BaseTile bt = (BaseTile) _tmgr.getTile(btid);
                        passable = bt.isPassable();
                    } catch (NoSuchTileSetException nstse) {
                        Log.warning("Autofringer couldn't find a base " +
                            "set while attempting to figure passability " +
                            "[error=" + nstse + "].");
                    }
                }
            }
        }

        // if nothing fringed, we're done
        int numfringers = _fringers.size();
        if (numfringers == 0) {
            return null;
        }

        // otherwise compose a FringeTile from the specified fringes
        FringerRec[] frecs = new FringerRec[numfringers];
        for (int ii = 0, pp = 0; ii < 16; ii++) {
            FringerRec rec = (FringerRec)_fringers.getValue(ii);
            if (rec != null) {
                frecs[pp++] = rec;
            }
        }

        BaseTile frTile = new BaseTile();
        frTile.setPassable(passable);
        composeFringeTile(frTile, frecs, masks, TileUtil.getTileHash(col, row));
        return frTile;
    }

    /**
     * Compose a FringeTile out of the various fringe images needed.
     */
    protected void composeFringeTile (
        Tile frTile, FringerRec[] fringers, HashMap masks, int hashValue)
    {
        // sort the array so that higher priority fringers get drawn first
        QuickSort.sort(fringers);

        BufferedImage ftimg = null;
        for (int ii = 0; ii < fringers.length; ii++) {
            int[] indexes = getFringeIndexes(fringers[ii].bits);
            for (int jj = 0; jj < indexes.length; jj++) {
                try {
                    ftimg = getTileImage(ftimg, fringers[ii].baseset,
                                         indexes[jj], masks, hashValue);
                } catch (NoSuchTileSetException nstse) {
                    Log.warning("Autofringer couldn't find a needed tileset " +
                                "[error=" + nstse + "].");
                }
            }
        }

        frTile.setImage(new BufferedMirage(ftimg));
    }

    /**
     * Retrieve or compose an image for the specified fringe.
     */
    protected BufferedImage getTileImage (
        BufferedImage ftimg, int baseset, int index,
        HashMap masks, int hashValue)
        throws NoSuchTileSetException
    {
        FringeConfiguration.FringeTileSetRecord tsr =
            _fringeconf.getFringe(baseset, hashValue);
        int fringeset = tsr.fringe_tsid;
        TileSet fset = _tmgr.getTileSet(fringeset);

        if (!tsr.mask) {
            // oh good, this is easy
            Tile stamp = fset.getTile(index);
            return stampTileImage(stamp, ftimg, stamp.getWidth(),
                                  stamp.getHeight());
        }

        // otherwise, it's a mask.. look for it in the cache..
        Long maskkey = Long.valueOf((((long) baseset) << 32) +
                                (fringeset << 16) + index);
        BufferedImage img = (BufferedImage)masks.get(maskkey);
        if (img == null) {
            BufferedImage fsrc = fset.getRawTileImage(index);
            BufferedImage bsrc = _tmgr.getTileSet(baseset).getRawTileImage(0);
            img = ImageUtil.composeMaskedImage(_imgr, fsrc, bsrc);
            masks.put(maskkey, img);
        }
        ftimg = stampTileImage(img, ftimg, img.getWidth(null),
                               img.getHeight(null));

        return ftimg;
    }

    /** Helper function for {@link #getTileImage}. */
    protected BufferedImage stampTileImage (Object stamp, BufferedImage ftimg,
                                            int width, int height)
    {
        // create the target image if necessary
        if (ftimg == null) {
            ftimg = _imgr.createImage(width, height, Transparency.BITMASK);
        }
        Graphics2D gfx = (Graphics2D)ftimg.getGraphics();
        try {
            if (stamp instanceof Tile) {
                ((Tile)stamp).paint(gfx, 0, 0);
            } else {
                gfx.drawImage((BufferedImage)stamp, 0, 0, null);
            }
        } finally {
            gfx.dispose();
        }
        return ftimg;
    }

    /**
     * Get the fringe index specified by the fringebits. If no index
     * is available, try breaking down the bits into contiguous regions of
     * bits and look for indexes for those.
     */
    protected int[] getFringeIndexes (int bits)
    {
        int index = BITS_TO_INDEX[bits];
        if (index != -1) {
            int[] ret = new int[1];
            ret[0] = index;
            return ret;
        }

        // otherwise, split the bits into contiguous components

        // look for a zero and start our first split
        int start = 0;
        while ((((1 << start) & bits) != 0) && (start < NUM_FRINGEBITS)) {
            start++;
        }

        if (start == NUM_FRINGEBITS) {
            // we never found an empty fringebit, and since index (above)
            // was already -1, we have no fringe tile for these bits.. sad.
            return new int[0];
        }

        ArrayList indexes = new ArrayList();
        int weebits = 0;
        for (int ii=(start + 1) % NUM_FRINGEBITS; ii != start;
             ii = (ii + 1) % NUM_FRINGEBITS) {

            if (((1 << ii) & bits) != 0) {
                weebits |= (1 << ii);
            } else if (weebits != 0) {
                index = BITS_TO_INDEX[weebits];
                if (index != -1) {
                    indexes.add(Integer.valueOf(index));
                }
                weebits = 0;
            }
        }
        if (weebits != 0) {
            index = BITS_TO_INDEX[weebits];
            if (index != -1) {
                indexes.add(Integer.valueOf(index));
            }
        }

        int[] ret = new int[indexes.size()];
        for (int ii=0; ii < ret.length; ii++) {
            ret[ii] = ((Integer) indexes.get(ii)).intValue();
        }
        return ret;
    }

    /**
     * A record for holding information about a particular fringe as we're
     * computing what it will look like.
     */
    static protected class FringerRec implements Comparable
    {
        int baseset;
        int priority;
        int bits;

        public FringerRec (int base, int pri)
        {
            baseset = base;
            priority = pri;
        }

        public int compareTo (Object o)
        {
            return priority - ((FringerRec) o).priority;
        }

        public String toString ()
        {
            return "[base=" + baseset + ", pri=" + priority +
                ", bits=" + Integer.toString(bits, 16) + "]";
        }
    }

    // fringe bits
    // see docs/miso/fringebits.png
    //
    protected static final int NORTH     = 1 << 0;
    protected static final int NORTHEAST = 1 << 1;
    protected static final int EAST      = 1 << 2;
    protected static final int SOUTHEAST = 1 << 3;
    protected static final int SOUTH     = 1 << 4;
    protected static final int SOUTHWEST = 1 << 5;
    protected static final int WEST      = 1 << 6;
    protected static final int NORTHWEST = 1 << 7;

    protected static final int NUM_FRINGEBITS = 8;

    // A matrix mapping adjacent tiles to which fringe bits 
    // they affect.
    // (x and y are offset by +1, since we can't have -1 as an array index)
    // again, see docs/miso/fringebits.png
    //
    protected static final int[][] FLAGMATRIX = {
        { NORTHEAST, (NORTHEAST | EAST | SOUTHEAST), SOUTHEAST },
        { (NORTHWEST | NORTH | NORTHEAST), 0, (SOUTHEAST | SOUTH | SOUTHWEST) },
        { NORTHWEST, (NORTHWEST | WEST | SOUTHWEST), SOUTHWEST }
    };

    /**
     * The fringe tiles we use. These are the 17 possible tiles made
     * up of continuous fringebits sections.
     * Huh? see docs/miso/fringebits.png
     */
    protected static final int[] FRINGETILES = {
        SOUTHEAST,
        SOUTHWEST | SOUTH | SOUTHEAST,
        SOUTHWEST,
        NORTHEAST | EAST | SOUTHEAST,
        NORTHWEST | WEST | SOUTHWEST,
        NORTHEAST,
        NORTHWEST | NORTH | NORTHEAST,
        NORTHWEST,

        SOUTHWEST | WEST | NORTHWEST | NORTH | NORTHEAST,
        NORTHWEST | NORTH | NORTHEAST | EAST | SOUTHEAST,
        NORTHWEST | WEST | SOUTHWEST | SOUTH | SOUTHEAST,
        SOUTHWEST | SOUTH | SOUTHEAST | EAST | NORTHEAST,

        NORTHEAST | NORTH | NORTHWEST | WEST | SOUTHWEST | SOUTH | SOUTHEAST,
        SOUTHEAST | EAST | NORTHEAST | NORTH | NORTHWEST | WEST | SOUTHWEST,
        SOUTHWEST | SOUTH | SOUTHEAST | EAST | NORTHEAST | NORTH | NORTHWEST,
        NORTHWEST | WEST | SOUTHWEST | SOUTH | SOUTHEAST | EAST | NORTHEAST,

        // all the directions!
        NORTH | NORTHEAST | EAST | SOUTHEAST |
                SOUTH | SOUTHWEST | WEST | NORTHWEST
    };

    // A reverse map of the above array, for quickly looking up which tile
    // we want.
    protected static final int[] BITS_TO_INDEX;

    // Construct the BITS_TO_INDEX array.
    static {
        int num = (1 << NUM_FRINGEBITS);
        BITS_TO_INDEX = new int[num];

        // first clear everything to -1 (meaning there is no tile defined)
        for (int ii=0; ii < num; ii++) {
            BITS_TO_INDEX[ii] = -1;
        }

        // then fill in with the defined tiles.
        for (int ii=0; ii < FRINGETILES.length; ii++) {
            BITS_TO_INDEX[FRINGETILES[ii]] = ii;
        }
    }

    protected ImageManager _imgr;
    protected TileManager _tmgr;
    protected FringeConfiguration _fringeconf;
    protected CheapIntMap _fringers = new CheapIntMap(16);
}
