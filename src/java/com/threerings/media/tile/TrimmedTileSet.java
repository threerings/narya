//
// $Id: TrimmedTileSet.java,v 1.2 2002/06/21 18:09:34 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;

import com.threerings.media.tile.util.TileSetTrimmer;

/**
 * Contains the necessary information to create a set of trimmed tiles
 * from a base image and the associated trim metrics.
 */
public class TrimmedTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        return _obounds.length;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex, Image tilesetImage)
    {
        // N/A
        return null;
    }

    // documentation inherited
    protected Tile createTile (int tileIndex, Image tilesetImage)
    {
        return new TrimmedTile(
            tilesetImage, _obounds[tileIndex], _tbounds[tileIndex]);
    }

    /**
     * Creates a trimmed tileset from the supplied source tileset. See
     * {@link TileSetTrimmer#trimTileSet} for further information.
     */
    public static TrimmedTileSet trimTileSet (
        TileSet source, OutputStream destImage)
        throws IOException
    {
        final TrimmedTileSet tset = new TrimmedTileSet();
        int tcount = source.getTileCount();

        // grab the dimensions of the original tiles
        tset._obounds = new Rectangle[tcount];
        for (int ii = 0; ii < tcount; ii++) {
            try {
                Tile tile = source.getTile(ii);
                tset._obounds[ii] = new Rectangle();
                tset._obounds[ii].width = tile.getWidth();
                tset._obounds[ii].height = tile.getHeight();
            } catch (NoSuchTileException nste) {
                String errmsg = "Urk! TileSet is ill-behaved. " +
                    "Claimed to have " + tcount + " tiles, but choked when " +
                    "we asked for tile " + ii + " [tset=" + source + "].";
                throw new RuntimeException(errmsg);
            }
        }
        tset._tbounds = new Rectangle[tcount];

        // create the trimmed tileset image
        TileSetTrimmer.TrimMetricsReceiver tmr =
            new TileSetTrimmer.TrimMetricsReceiver() {
                public void trimmedTile (int tileIndex, int imageX, int imageY,
                                         int trimX, int trimY,
                                         int trimWidth, int trimHeight) {
                    tset._obounds[tileIndex].x = imageX;
                    tset._obounds[tileIndex].y = imageY;
                    tset._tbounds[tileIndex] =
                        new Rectangle(trimX, trimY, trimWidth, trimHeight);
                }
            };
        TileSetTrimmer.trimTileSet(source, destImage, tmr);

        return tset;
    }

    /** The width and height of the untrimmed tile, and the x and y offset
     * of the trimmed image within our tileset image. */
    protected Rectangle[] _obounds;

    /** The width and height of the trimmed image and the x and y offset
     * within the untrimmed image at which the trimmed image should be
     * rendered. */
    protected Rectangle[] _tbounds;
}
