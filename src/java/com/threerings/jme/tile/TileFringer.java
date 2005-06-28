//
// $Id: TileFringer.java 3177 2004-10-28 17:49:02Z mdb $
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

package com.threerings.jme.tile;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.QuickSort;

import com.threerings.media.image.ImageUtil;
import com.threerings.media.tile.TileUtil;

import com.threerings.jme.Log;

/**
 * Computes fringe tile images according to the rules in an associated
 * fringe configuration.
 */
public class TileFringer
{
    public static interface TileSource
    {
        /** Returns the type of tile at the specified coordinates or -1 if
         * there is no tile at this coordinate. */
        public String getTileType (int x, int y);

        /** Returns the tile type to use when a coordinate has no tile. */
        public String getDefaultType ();
    }

    public static interface ImageSource extends ImageUtil.ImageCreator
    {
        /** Creates a blank image into which various fringe images will be
         * composited. */
        public BufferedImage createImage (
            int width, int height, int transparency);

        /** Returns the source image for a tile of the specified type.
         * This can be randomly selected and change from call to call. */
        public BufferedImage getTileSource (String type);

        /** Returns the named fringe source image (one long strip). */
        public BufferedImage getFringeSource (String name);
    }

    /**
     * Creates a fringer that will fringe according to the rules in the
     * supplied configuration.
     */
    public TileFringer (FringeConfiguration config, ImageSource isrc)
    {
        _config = config;
        _isrc = isrc;
    }

    /**
     * Computes, creates and returns the base tile with the appropriate
     * fringe imagery applied to it for the specified location.
     *
     * @param masks used to cache intermediate images of tiles cut out
     * using a fringe mask.
     */
    public BufferedImage getFringeTile (
        TileSource tiles, int col, int row, HashMap masks)
    {
        // get the type of the tile we are considering
        String baseType = tiles.getTileType(col, row);

        // start with an empty fringer list
        FringerRec fringers = null;

        // walk through our influence tiles
        for (int y = row - 1, maxy = row + 2; y < maxy; y++) {
            for (int x = col - 1, maxx = col + 2; x < maxx; x++) {
                // we sensibly do not consider ourselves
                if ((x == col) && (y == row)) {
                    continue;
                }

                // determine the type of our fringing neighbor
                String fringerType = tiles.getTileType(x, y);
                if (fringerType == null) {
                    fringerType = tiles.getDefaultType();
                }

                // determine if it fringes on our tile
                int pri = _config.fringesOn(fringerType, baseType);
                if (pri == -1) {
                    continue;
                }

                FringerRec fringer = (fringers == null) ?
                    null : fringers.find(fringerType);
                if (fringer == null) {
                    fringer = fringers =
                        new FringerRec(fringerType, pri, fringers);
                }

                // now turn on the appropriate fringebits
                int dy = y - row, dx = x - col;
                fringer.bits |= FLAGMATRIX[dy*3+dx+4];
            }
        }

        // if nothing fringed, we're done
        if (fringers == null) {
            return null;
        }

        // otherwise compose a fringe tile from the specified fringes
        return composeFringeTile(
            baseType, fringers.toArray(), masks, TileUtil.getTileHash(col, row));
    }

    /**
     * Compose a fringe tile out of the various fringe images needed.
     */
    protected BufferedImage composeFringeTile (
        String baseType, FringerRec[] fringers, HashMap masks, int hashValue)
    {
        // sort the array so that higher priority fringers get drawn first
        QuickSort.sort(fringers);

        BufferedImage source = _isrc.getTileSource(baseType);
        BufferedImage ftimg = _isrc.createImage(
            source.getWidth(), source.getHeight(), Transparency.OPAQUE);
        Graphics2D gfx = (Graphics2D)ftimg.getGraphics();
        try {
            // start with the base tile image
            gfx.drawImage(source, 0, 0, null);

            // and stamp the fringers on top of it
            for (int ii = 0; ii < fringers.length; ii++) {
                int[] indexes = getFringeIndexes(fringers[ii].bits);
                for (int jj = 0; jj < indexes.length; jj++) {
                    stampTileImage(gfx, fringers[ii].fringerType,
                                   indexes[jj], masks, hashValue);
                }
            }

        } finally {
            gfx.dispose();
        }
        return ftimg;
    }

    /**
     * Looks up or creates the appropriate fringe mask and draws it into
     * the supplied graphics context.
     */
    protected void stampTileImage (
        Graphics2D gfx, String fringerType, int index,
        HashMap masks, int hashValue)
    {
        FringeConfiguration.FringeRecord frec =
            _config.getFringe(fringerType, hashValue);
        BufferedImage fsimg = (frec == null) ? null :
            _isrc.getFringeSource(frec.name);
        if (fsimg == null) {
            Log.warning("Missing fringe source image [type=" + fringerType +
                        ", hash=" + hashValue + ", frec=" + frec + "].");
            return;
        }

        if (frec.mask) {
            // it's a mask; look for it in the cache
            String maskkey = fringerType + ":" + frec.name + ":" + index;
            BufferedImage mimg = (BufferedImage)masks.get(maskkey);
            if (mimg == null) {
                BufferedImage fsrc = getSubimage(fsimg, index);
                BufferedImage bsrc = _isrc.getTileSource(fringerType);
                mimg = ImageUtil.composeMaskedImage(_isrc, fsrc, bsrc);
                masks.put(maskkey, mimg);
            }
            gfx.drawImage(mimg, 0, 0, null);

        } else {
            // this is a non-mask image so just use the data from the
            // fringe source image directly
            gfx.drawImage(getSubimage(fsimg, index), 0, 0, null);
        }
    }

    /**
     * Returns the <code>index</code>th tile image from the supplied
     * source image. The source image is assumed to be a single strip of
     * tile images, each with equal width and height.
     */
    protected BufferedImage getSubimage (BufferedImage source, int index)
    {
        int size = source.getHeight(), x = size * index;
        return source.getSubimage(x, 0, size, size);
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
                    indexes.add(new Integer(index));
                }
                weebits = 0;
            }
        }
        if (weebits != 0) {
            index = BITS_TO_INDEX[weebits];
            if (index != -1) {
                indexes.add(new Integer(index));
            }
        }

        int[] ret = new int[indexes.size()];
        for (int ii=0; ii < ret.length; ii++) {
            ret[ii] = ((Integer) indexes.get(ii)).intValue();
        }
        return ret;
    }

    /** A record for holding information about a particular fringe as
     * we're computing what it will look like. */
    protected static class FringerRec implements Comparable
    {
        public String fringerType;
        public int priority;
        public int bits;
        public FringerRec next;

        public FringerRec (String type, int pri, FringerRec next) {
            fringerType = type;
            priority = pri;
            this.next = next;
        }

        public FringerRec find (String type)
        {
            if (fringerType.equals(type)) {
                return this;
            } else if (next != null) {
                return next.find(type);
            } else {
                return null;
            }
        }

        public FringerRec[] toArray ()
        {
            return toArray(0);
        }

        public int compareTo (Object o) {
            return priority - ((FringerRec) o).priority;
        }

        public String toString () {
            return "[type=" + fringerType + ", pri=" + priority +
                ", bits=" + Integer.toString(bits, 16) + "]";
        }

        protected FringerRec[] toArray (int index)
        {
            FringerRec[] array;
            if (next == null) {
                array = new FringerRec[index+1];
            } else {
                array = next.toArray(index+1);
            }
            array[index] = this;
            return array;
        }
    }

    protected static final int NORTH     = 1 << 0;
    protected static final int NORTHEAST = 1 << 1;
    protected static final int EAST      = 1 << 2;
    protected static final int SOUTHEAST = 1 << 3;
    protected static final int SOUTH     = 1 << 4;
    protected static final int SOUTHWEST = 1 << 5;
    protected static final int WEST      = 1 << 6;
    protected static final int NORTHWEST = 1 << 7;

    protected static final int NUM_FRINGEBITS = 8;

    /** A matrix mapping adjacent tiles to which fringe bits they affect.
     * (x and y are offset by +1, since we can't have -1 as an array index).
     * These are "upside down" thanks to OpenGL. */
    protected static final int[] FLAGMATRIX = {
        SOUTHWEST, (SOUTHEAST | SOUTH | SOUTHWEST), SOUTHEAST,
        (NORTHWEST | WEST | SOUTHWEST), 0, (NORTHEAST | EAST | SOUTHEAST),
        NORTHWEST, (NORTHWEST | NORTH | NORTHEAST), NORTHEAST,
    };

    /** The fringe tiles we use. These are the 17 possible tiles made up
     * of continuous fringebits sections. */
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
        NORTH | NORTHEAST | EAST | SOUTHEAST | SOUTH | SOUTHWEST |
        WEST | NORTHWEST
    };

    /** A reverse map of the {@link #FRINGETILES} array, for quickly
     * looking up which tile we want. */
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

    protected ImageSource _isrc;
    protected FringeConfiguration _config;
}
