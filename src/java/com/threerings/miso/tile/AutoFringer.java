//
// $Id: AutoFringer.java,v 1.1 2002/04/06 01:53:58 ray Exp $

package com.threerings.miso.tile;

import java.util.Random;

import com.samskivert.util.HashIntMap;

import com.threerings.media.Log;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.MaskedUniformTileSet;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileLayer;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.tile.UniformTileSet;

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
        fringe(scene, fringelayer, 0, 0, scene.width, scene.height);
    }

    /**
     * Automatically generate fringe information for the specified rectangular
     * region AND THE TILES THEY INFLUENCE and insert into the fringe TileLayer.
     */
    public void fringe (MisoSceneModel scene, TileLayer fringelayer,
                        Rectangle r) 
              // int startx, int starty, int width, int height)
    {
        // create a hash to cache our masks
        HashIntMap maskcache = new HashIntMap();

        int lastrow = Math.min(r.y + r.height + 1, _scene.height);
        int lastcol = Math.min(r.x + r.width + 1, _scene.width);

        for (int row = Math.max(r.y - 1, 0); row < lastrow; row++) {
            for (int col = Math.max(r.x - 1); col < lastcol; col++) {
                fringelayer.setTile(col, row,
                                    getFringeTile(scene, row, col, maskcache));
            }
        }

        // and then we throw maskcache out...
    }

    /**
     * The actual computation of fringe for a specified location.
     *
     * @return a fully qualified fringe tileid for the specified location
     * (0 for no fringe)
     */
    protected Tile getFringeTile (MisoSceneModel scene, int row, int col,
                                  HashIntMap masks)
    {
        int hei = scene.height;
        int wid = scene.width;

        // get the tileset id of the base tile we are considering
        int underset = scene.baseTileIds[row * wid + col] >> 16;

        // hold the tileset of the base tile that will fringe on us
        // (initially set to bogus value)
        int overset = -1;

        // the pieces of fringe that are turned on by influencing tiles
        int fringebits = 0;

        // walk through our influence tiles
        for (int y=Math.max(0, row - 1); y < Math.min(hei, row + 2); y++) {

            for (int x=Math.max(0, col - 1); x < Math.min(wid, col + 2); x++) {

                // we sensibly do not consider ourselves
                if ((x == col) && (y == row)) {
                    continue;
                }

                int baseset = scene.baseTileIds[y * wid + x] >> 16;

                // if this set does not fringe on us, move on to the next
                if (!_fringeconf.fringesOn(baseset, underset)) {
                    continue;
                }

                if (overset == -1) {
                    // if this is the first fringer we've seen
                    // remember that fact
                    overset = baseset;
                } else if (overset != baseset) {
                    // oh no! two different fringes want to fringe this
                    // tile. We don't support that, so instead: no fringe!
                    Log.debug("Two different base tilesets affect fringe " +
                              "and so we fail with no fringe [x=" + col +
                              ", y=" + row + "].");

                    return null;
                }

                // now turn on the appropriate fringebits
                fringebits |= FLAGMATRIX[y - row + 1][x - col + 1];
            }
        }

        // now we've looked at all the influencing tiles

        // look up the appropriate fringe index according to which bits
        // are turned on
        int index = _fringeconf.getTileIndexFromFringeBits(fringebits);
        if (index == -1) {

            // our fringes do not specify a tile to use in this case.
            return null;
        }

        try {
            return getTile(overset, index, masks);
        } catch (NoSuchTileException nste) {
            Log.warning("Autofringer couldn't find a needed tile.");
            return null;
        } catch (NoSuchTileSetException nstse) {
            Log.warning("Autofringer couldn't find a needed tileset.");
            return null;
        }
    }

    protected Tile getTile (int baseset, int index, HashIntMap masks)
        throws NoSuchTileException, NoSuchTileSetException
    {
        FringeConfiguration.FringeTileSetRecord tsr =
            _fringeconf.getRandomFringe(baseset, rando);

        int fringeset = tsr.fringe_tsid;

        if (!tsr.mask) {
            // oh good, this is easy.
            return _tmgr.getTile(fringeset, index);
        }

        // otherwise, it's a mask.. look for it in the cache..
        int maskkey = (baseset << 16) + fringeset;

        TileSet tset = (TileSet) masks.get(maskkey);
        if (tset == null) {
            tset = new MaskedUniformTileSet(_tmgr.getTile(baseset, 0),
                                            _tmgr.getTileSet(fringeset));
            masks.put(maskkey, tset);
            Log.debug("created cached set");
        }

        return tset.getTile(index);
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
