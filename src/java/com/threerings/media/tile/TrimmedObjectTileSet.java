//
// $Id: TrimmedObjectTileSet.java,v 1.6 2003/02/05 00:22:05 mdb Exp $

package com.threerings.media.tile;

import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.image.Mirage;
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

    /**
     * Returns the x coordinate of the spot associated with the specified
     * tile index.
     */
    public int getXSpot (int tileIdx)
    {
        return (_bits == null) ? 0 : _bits[tileIdx].xspot;
    }

    /**
     * Returns the y coordinate of the spot associated with the specified
     * tile index.
     */
    public int getYSpot (int tileIdx)
    {
        return (_bits == null) ? 0 : _bits[tileIdx].yspot;
    }

    /**
     * Returns the orientation of the spot associated with the specified
     * tile index.
     */
    public int getSpotOrient (int tileIdx)
    {
        return (_bits == null) ? 0 : _bits[tileIdx].sorient;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex)
    {
        return _bounds[tileIndex];
    }

    // documentation inherited
    protected Tile createTile (int tileIndex, Mirage tileImage)
    {
        ObjectTile tile = new ObjectTile(tileImage);
        tile.setBase(_ometrics[tileIndex].width, _ometrics[tileIndex].height);
        tile.setOrigin(_ometrics[tileIndex].x, _ometrics[tileIndex].y);
        if (_bits != null) {
            tile.setPriority(_bits[tileIndex].priority);
        }
        return tile;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", ometrics=").append(StringUtil.toString(_ometrics));
	buf.append(", bounds=").append(StringUtil.toString(_bounds));
	buf.append(", bits=").append(StringUtil.toString(_bits));
	buf.append(", zations=").append(StringUtil.toString(_zations));
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

//         System.out.println("Trimming object tile set " +
//                            "[source=" + source + "].");

        // create our metrics arrays
        tset._bounds = new Rectangle[tcount];
        tset._ometrics = new Rectangle[tcount];

        // create our bits if needed
        if (source._priorities != null ||
            source._xspots != null) {
            tset._bits = new Bits[tcount];
        }

        // copy our colorizations
        tset._zations = source.getColorizations();

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

            // fill in our bits
            if (tset._bits != null) {
                tset._bits[ii] = new Bits();
            }
            if (source._priorities != null) {
                tset._bits[ii].priority = source._priorities[ii];
            }
            if (source._xspots != null) {
                tset._bits[ii].xspot = source._xspots[ii];
                tset._bits[ii].yspot = source._yspots[ii];
                tset._bits[ii].sorient = source._sorients[ii];
            }
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

    /** Extra bits related to object tiles. */
    protected static class Bits implements Serializable
    {
        /** The default render priority for this object. */
        public byte priority;

        /** The x coordinate of the "spot" associated with this object. */
        public short xspot;

        /** The y coordinate of the "spot" associated with this object. */
        public short yspot;

        /** The orientation of the "spot" associated with this object. */
        public short sorient;

        /** Generates a string representation of this instance. */
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 1;
    }

    /** Contains the width and height of each object tile and the offset
     * into the tileset image of their image data. */
    protected Rectangle[] _bounds;

    /** Contains the origin offset for each object tile and the object
     * footprint width and height (in tile units). */
    protected Rectangle[] _ometrics;

    /** Extra bits relating to our objects. */
    protected Bits[] _bits;

    /** Colorization classes that apply to our objects. */
    protected String[] _zations;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
