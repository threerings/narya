//
// $Id: TrimmedObjectTileSet.java,v 1.1 2002/06/21 18:53:13 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.tile.util.TileSetTrimmer;

/**
 * An object tileset in which the objects have been trimmed to the
 * smallest possible images that still contain all of their
 * non-transparent pixels. The objects' origins are adjusted so that the
 * objects otherwise behave exactly as the untrimmed objects and are thus
 * interchangeable (and more memory efficient).
 */
public class TrimmedObjectTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        return _bounds.length;
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
        ObjectTile tile = new ObjectTile(tilesetImage, _bounds[tileIndex]);
        tile.setBase(_ometrics[tileIndex].width, _ometrics[tileIndex].height);
        tile.setOrigin(_ometrics[tileIndex].x, _ometrics[tileIndex].y);
        return tile;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", ometrics=").append(StringUtil.toString(_ometrics));
	buf.append(", bounds=").append(StringUtil.toString(_bounds));
    }

    /**
     * Creates a trimmed object tileset from the supplied source object
     * tileset. The image path must be set by hand to the appropriate path
     * based on where the image data that is written to the
     * <code>destImage</code> parameter is actually stored on the file
     * system. See {@link TileSetTrimmer#trimTileSet} for further
     * information.
     */
    public static TrimmedObjectTileSet trimObjectTileSet (
        ObjectTileSet source, OutputStream destImage)
        throws IOException
    {
        final TrimmedObjectTileSet tset = new TrimmedObjectTileSet();
        tset.setName(source.getName());
        int tcount = source.getTileCount();

        // create our metrics arrays
        tset._bounds = new Rectangle[tcount];
        tset._ometrics = new Rectangle[tcount];

        // fill in the original object metrics
        for (int ii = 0; ii < tcount; ii++) {
            tset._ometrics[ii] = new Rectangle();
            if (source._xorigins != null) { 
                tset._ometrics[ii].x = source._xorigins[ii];
            }
            if (source._yorigins != null) { 
                tset._ometrics[ii].y = source._yorigins[ii];
            }
            tset._ometrics[ii].width = source._owidths[ii];
            tset._ometrics[ii].height = source._oheights[ii];
        }

        // create the trimmed tileset image
        TileSetTrimmer.TrimMetricsReceiver tmr =
            new TileSetTrimmer.TrimMetricsReceiver() {
                public void trimmedTile (int tileIndex, int imageX, int imageY,
                                         int trimX, int trimY,
                                         int trimWidth, int trimHeight) {
                    tset._ometrics[tileIndex].x -= trimX;
                    tset._ometrics[tileIndex].y -= trimY;
                    tset._bounds[tileIndex] =
                        new Rectangle(imageX, imageY, trimWidth, trimHeight);
                }
            };
        TileSetTrimmer.trimTileSet(source, destImage, tmr);

//         Log.info("Trimmed object tileset " +
//                  "[bounds=" + StringUtil.toString(tset._bounds) +
//                  ", metrics=" + StringUtil.toString(tset._ometrics) + "].");

        return tset;
    }

    /** Contains the width and height of each object tile and the offset
     * into the tileset image of their image data. */
    protected Rectangle[] _bounds;

    /** Contains the origin offset for each object tile and the object
     * footprint width and height (in tile units). */
    protected Rectangle[] _ometrics;
}
