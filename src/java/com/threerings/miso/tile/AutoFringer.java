//
// $Id: AutoFringer.java,v 1.6 2002/04/06 22:07:41 ray Exp $

package com.threerings.miso.tile;

import java.awt.Rectangle;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.HashMap;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.QuickSort;

import com.threerings.media.Log;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.tile.UniformTileSet;

import com.threerings.media.util.ImageUtil;

import com.threerings.miso.scene.MisoSceneModel;

/**
 * Automatically fringes a scene according to the rules in the
 * FringeConfiguration.
 */
public class AutoFringer
{
    /**
     * Construct an AutoFringer
     */
    public AutoFringer (FringeConfiguration fringeconf, TileManager tmgr)
    {
        _fringeconf = fringeconf;
        _tmgr = tmgr;
    }

    /**
     * Automatically fringe the entire scene.
     */
    public void fringe (MisoSceneModel scene, TileLayer fringelayer)
    {
        fringe(scene, fringelayer,
               new Rectangle(0, 0, scene.width, scene.height));
    }

    /**
     * Automatically generate fringe information for the specified
     * rectangular region <strong>and the tiles they influence</strong>
     * and insert into the fringe TileLayer.
     */
    public void fringe (MisoSceneModel scene, TileLayer fringelayer,
                        Rectangle r) 
    {
        // create a hash to cache our masks
        HashMap maskcache = new HashMap();

        int lastrow = Math.min(r.y + r.height + 1, scene.height);
        int lastcol = Math.min(r.x + r.width + 1, scene.width);

        for (int row = Math.max(r.y - 1, 0); row < lastrow; row++) {
            for (int col = Math.max(r.x - 1, 0); col < lastcol; col++) {
                fringelayer.setTile(col, row,
                                    getFringeTile(scene, row, col, maskcache));
            }
        }

        // and then we throw maskcache out...
    }

    /**
     * Compute and return the fringe Tile to be inserted at the specified
     * location.
     */
    protected Tile getFringeTile (MisoSceneModel scene, int row, int col,
                                  HashMap masks)
    {
        HashIntMap fringers = new HashIntMap();
        int hei = scene.height;
        int wid = scene.width;

        // get the tileset id of the base tile we are considering
        int underset = scene.baseTileIds[row * wid + col] >> 16;

        // walk through our influence tiles
        for (int y=Math.max(0, row - 1); y < Math.min(hei, row + 2); y++) {

            for (int x=Math.max(0, col - 1); x < Math.min(wid, col + 2); x++) {

                // we sensibly do not consider ourselves
                if ((x == col) && (y == row)) {
                    continue;
                }

                int baseset = scene.baseTileIds[y * wid + x] >> 16;

                int pri = _fringeconf.fringesOn(baseset, underset);
                if (pri == -1) {
                    continue;
                }

                FringerRec fringer = (FringerRec) fringers.get(baseset);
                if (fringer == null) {
                    fringer = new FringerRec(baseset, pri);
                    fringers.put(baseset, fringer);
                }

                // now turn on the appropriate fringebits
                fringer.bits |= FLAGMATRIX[y - row + 1][x - col + 1];
            }
        }

        int numfringers = fringers.size();
        // if nothing fringed, we're done.
        if (numfringers == 0) {
            return null;
        }

        // otherwise compose a FringeTile from the specified fringes
        FringerRec[] frecs = new FringerRec[numfringers];
        Iterator iter = fringers.elements();
        for (int ii=0; ii < frecs.length; ii++) {
            frecs[ii] = (FringerRec) iter.next();
        }

        return composeFringeTile(frecs, masks);
    }

    /**
     * Compose a FringeTile out of the various fringe images needed.
     */
    protected Tile composeFringeTile (FringerRec[] fringers, HashMap masks)
    {
        // sort the array so that higher priority fringers get drawn first
        QuickSort.sort(fringers);

        FringeTile tile = null;

        for (int ii=0; ii < fringers.length; ii++) {
            int[] indexes = getFringeIndexes(fringers[ii].bits);
            for (int jj=0; jj < indexes.length; jj++) {

                try {
                    Image fimg = getTileImage(fringers[ii].baseset,
                                              indexes[jj], masks);

                    if (tile == null) {
                        tile = new FringeTile(fimg);
                    } else {
                        tile.addExtraImage(fimg);
                    }

                } catch (NoSuchTileException nste) {
                    Log.warning("Autofringer couldn't find a needed tile " +
                                "[error=" + nste + "].");
                } catch (NoSuchTileSetException nstse) {
                    Log.warning("Autofringer couldn't find a needed tileset " +
                                "[error=" + nstse + "].");
                }
            }
        }

        return tile;
    }

    /**
     * Retrieve or compose an image for the specified fringe.
     */
    protected Image getTileImage (int baseset, int index, HashMap masks)
        throws NoSuchTileException, NoSuchTileSetException
    {
        FringeConfiguration.FringeTileSetRecord tsr =
            _fringeconf.getRandomFringe(baseset, rando);

        int fringeset = tsr.fringe_tsid;

        if (!tsr.mask) {
            // oh good, this is easy.
            return _tmgr.getTile(fringeset, index).getImage();
        }

        // otherwise, it's a mask.. look for it in the cache..
        Long maskkey = new Long(
            (((long) baseset) << 32) + (fringeset << 16) + index);

        Image img = (Image) masks.get(maskkey);
        if (img == null) {
            img = ImageUtil.composeMaskedImage(
                (BufferedImage) _tmgr.getTile(fringeset, index).getImage(),
                (BufferedImage) _tmgr.getTile(baseset, 0).getImage());

            masks.put(maskkey, img);
        }

        return img;
    }

    /**
     * Get the fringe index specified by the fringebits. If no index
     * is available, try breaking down the bits into contiguous regions of
     * bits and look for indexes for those.
     */
    protected int[] getFringeIndexes (int bits)
    {
        int index = _fringeconf.getTileIndexFromFringeBits(bits);
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
                index = _fringeconf.getTileIndexFromFringeBits(weebits);
                if (index != -1) {
                    indexes.add(new Integer(index));
                }
                weebits = 0;
            }
        }
        if (weebits != 0) {
            index = _fringeconf.getTileIndexFromFringeBits(weebits);
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

    /**
     * A record for holding information about a particular fringe as we're
     * computing what it will look like.
     */
    static private class FringerRec implements Comparable
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
    }

    // fringe bits
    // see docs/miso/fringebits.png
    //
    private static final int NORTH     = 1 << 0;
    private static final int NORTHEAST = 1 << 1;
    private static final int EAST      = 1 << 2;
    private static final int SOUTHEAST = 1 << 3;
    private static final int SOUTH     = 1 << 4;
    private static final int SOUTHWEST = 1 << 5;
    private static final int WEST      = 1 << 6;
    private static final int NORTHWEST = 1 << 7;

    private static final int NUM_FRINGEBITS = 8;

    // A matrix mapping adjacent tiles to which fringe bits 
    // they affect.
    // (x and y are offset by +1, since we can't have -1 as an array index)
    // again, see docs/miso/fringebits.png
    //
    private static final int[][] FLAGMATRIX = {
        { NORTHEAST, (NORTHEAST | EAST | SOUTHEAST), SOUTHEAST },
        { (NORTHWEST | NORTH | NORTHEAST), 0, (SOUTHEAST | SOUTH | SOUTHWEST) },
        { NORTHWEST, (NORTHWEST | WEST | SOUTHWEST), SOUTHWEST }
    };

    /** Our tile manager. */
    protected static TileManager _tmgr;

    /** Our fringe configuration. */
    protected static FringeConfiguration _fringeconf;

    /** Our random # generator. */
    // this may change.. or we may seed it before we do any scene
    // with a number deterministicly generated from that scene
    protected static Random rando = new Random();
}
