//
// $Id: AutoFringer.java,v 1.18 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.QuickSort;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.image.BufferedMirage;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.tile.UniformTileSet;

import com.threerings.media.image.ImageUtil;

import com.threerings.media.image.BackedVolatileMirage;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;

import com.threerings.miso.data.MisoSceneModel;

/**
 * Automatically fringes a scene according to the rules in the
 * FringeConfiguration.
 */
public class AutoFringer
{
    /**
     * Construct an AutoFringer
     */
    public AutoFringer (FringeConfiguration fringeconf, ImageManager imgr,
                        TileManager tmgr)
    {
        _fringeconf = fringeconf;
        _imgr = imgr;
        _tmgr = tmgr;
    }

    /**
     * Automatically fringe the entire scene.
     */
    public void fringe (MisoSceneModel scene, TileLayer fringelayer, long seed)
    {
        fringe(scene, fringelayer, new Random(seed));
    }

    /**
     * Automatically fringe the entire scene.
     */
    public void fringe (
        MisoSceneModel scene, TileLayer fringelayer, Random rando)
    {
        fringe(scene, fringelayer,
               new Rectangle(0, 0, scene.width, scene.height), rando);
    }

    /**
     * Automatically generate fringe information for the specified
     * rectangular region <strong>and the tiles they influence</strong>
     * and insert into the fringe TileLayer.
     */
    public void fringe (MisoSceneModel scene, TileLayer fringelayer,
                        Rectangle r, Random rando) 
    {
        long start = System.currentTimeMillis();

        // create a hash to cache our masks
        HashMap maskcache = new HashMap();

        int lastrow = Math.min(r.y + r.height + 1, scene.height);
        int lastcol = Math.min(r.x + r.width + 1, scene.width);

        for (int row = Math.max(r.y - 1, 0); row < lastrow; row++) {
            for (int col = Math.max(r.x - 1, 0); col < lastcol; col++) {
                Tile tile = getFringeTile(scene, row, col, maskcache, rando);
                fringelayer.setTile(col, row, tile);
            }
        }

        // and then we throw maskcache out...
//         long now = System.currentTimeMillis();
//         long size = ImageUtil.getEstimatedMemoryUsage(
//             maskcache.values().iterator());
//         Log.debug("Finished fringing scene [ms=" + (now - start) +
//                   ", mem=" + (size / 1024) + "k" +
//                   ", size=" + maskcache.size() + "].");
    }

    /**
     * Compute and return the fringe Tile to be inserted at the specified
     * location.
     */
    protected Tile getFringeTile (MisoSceneModel scene, int row, int col,
                                  HashMap masks, Random rando)
    {
        HashIntMap fringers = new HashIntMap();
        int hei = scene.height;
        int wid = scene.width;

        // get the tileset id of the base tile we are considering
        int underset = scene.getBaseTile(col, row) >> 16;

        // walk through our influence tiles
        int maxy = Math.min(hei, row + 2);
        int maxx = Math.min(wid, col + 2);
        for (int y = Math.max(0, row - 1); y < maxy; y++) {
            for (int x = Math.max(0, col - 1); x < maxx; x++) {
                // we sensibly do not consider ourselves
                if ((x == col) && (y == row)) {
                    continue;
                }

                int baseset = scene.getBaseTile(x, y) >> 16;
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

        // if nothing fringed, we're done.
        int numfringers = fringers.size();
        if (numfringers == 0) {
            return null;
        }

        // otherwise compose a FringeTile from the specified fringes
        FringerRec[] frecs = new FringerRec[numfringers];
        Iterator iter = fringers.elements();
        for (int ii=0; ii < frecs.length; ii++) {
            frecs[ii] = (FringerRec) iter.next();
        }

        return composeFringeTile(frecs, masks, rando);
    }

    /**
     * Compose a FringeTile out of the various fringe images needed.
     */
    protected Tile composeFringeTile (
        FringerRec[] fringers, HashMap masks, Random rando)
    {
        // sort the array so that higher priority fringers get drawn first
        QuickSort.sort(fringers);

        BufferedImage ftimg = null;
        for (int ii = 0; ii < fringers.length; ii++) {
            int[] indexes = getFringeIndexes(fringers[ii].bits);
            for (int jj = 0; jj < indexes.length; jj++) {
                try {
                    ftimg = getTileImage(ftimg, fringers[ii].baseset,
                                         indexes[jj], masks, rando);
                } catch (NoSuchTileException nste) {
                    Log.warning("Autofringer couldn't find a needed tile " +
                                "[error=" + nste + "].");
                } catch (NoSuchTileSetException nstse) {
                    Log.warning("Autofringer couldn't find a needed tileset " +
                                "[error=" + nstse + "].");
                }
            }
        }

        return new Tile(new BufferedMirage(ftimg));
    }

    /**
     * Retrieve or compose an image for the specified fringe.
     */
    protected BufferedImage getTileImage (
        BufferedImage ftimg, int baseset, int index,
        HashMap masks, Random rando)
        throws NoSuchTileException, NoSuchTileSetException
    {
        FringeConfiguration.FringeTileSetRecord tsr =
            _fringeconf.getRandomFringe(baseset, rando);
        int fringeset = tsr.fringe_tsid;
        TileSet fset = _tmgr.getTileSet(fringeset);

        if (!tsr.mask) {
            // oh good, this is easy
            Tile stamp = fset.getTile(index);
            return stampTileImage(stamp, ftimg, stamp.getWidth(),
                                  stamp.getHeight());
        }

        // otherwise, it's a mask.. look for it in the cache..
        Long maskkey = new Long((((long) baseset) << 32) +
                                (fringeset << 16) + index);
        BufferedImage img = (BufferedImage)masks.get(maskkey);
        if (img == null) {
            BufferedImage fsrc = fset.getTileImage(index);
            BufferedImage bsrc = _tmgr.getTileSet(baseset).getTileImage(0);
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
}
