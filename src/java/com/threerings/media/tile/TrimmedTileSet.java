//
// $Id: TrimmedTileSet.java,v 1.6 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;

import com.threerings.media.image.Mirage;
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
    protected Rectangle computeTileBounds (int tileIndex)
    {
        return _obounds[tileIndex];
    }

    // documentation inherited
    protected Tile createTile (int tileIndex, Mirage image)
    {
        return new TrimmedTile(image, _tbounds[tileIndex]);
    }

    /**
     * Creates a trimmed tileset from the supplied source tileset. The
     * image path must be set by hand to the appropriate path based on
     * where the image data that is written to the <code>destImage</code>
     * parameter is actually stored on the file system. See {@link
     * TileSetTrimmer#trimTileSet} for further information.
     */
    public static TrimmedTileSet trimTileSet (
        TileSet source, OutputStream destImage)
        throws IOException
    {
        final TrimmedTileSet tset = new TrimmedTileSet();
        tset.setName(source.getName());
        int tcount = source.getTileCount();
        tset._tbounds = new Rectangle[tcount];
        tset._obounds = new Rectangle[tcount];

        // grab the dimensions of the original tiles
        for (int ii = 0; ii < tcount; ii++) {
            tset._tbounds[ii] = source.computeTileBounds(ii);
        }

        // create the trimmed tileset image
        TileSetTrimmer.TrimMetricsReceiver tmr =
            new TileSetTrimmer.TrimMetricsReceiver() {
                public void trimmedTile (int tileIndex, int imageX, int imageY,
                                         int trimX, int trimY,
                                         int trimWidth, int trimHeight) {
                    tset._tbounds[tileIndex].x = trimX;
                    tset._tbounds[tileIndex].y = trimY;
                    tset._obounds[tileIndex] =
                        new Rectangle(imageX, imageY, trimWidth, trimHeight);
                }
            };
        TileSetTrimmer.trimTileSet(source, destImage, tmr);

        return tset;
    }

    /** The width and height of the trimmed tile, and the x and y offset
     * of the trimmed image within our tileset image. */
    protected Rectangle[] _obounds;

    /** The width and height of the untrimmed image and the x and y offset
     * within the untrimmed image at which the trimmed image should be
     * rendered. */
    protected Rectangle[] _tbounds;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
