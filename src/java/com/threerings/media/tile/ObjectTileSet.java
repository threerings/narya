//
// $Id: ObjectTileSet.java,v 1.6 2001/12/18 08:37:54 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.util.Arrays;

import com.samskivert.util.StringUtil;
import com.threerings.media.ImageManager;

/**
 * The object tileset supports the specification of object information for
 * object tiles in addition to all of the features of the swiss army
 * tileset.
 *
 * @see ObjectTile
 */
public class ObjectTileSet extends SwissArmyTileSet
{
    /**
     * Sets the widths (in unit tile count) of the objects in this
     * tileset. This must be accompanied by a call to {@link
     * #setObjectHeights}.
     */
    public void setObjectWidths (int[] objectWidths)
    {
        _owidths = objectWidths;
    }

    /**
     * Sets the heights (in unit tile count) of the objects in this
     * tileset. This must be accompanied by a call to {@link
     * #setObjectWidths}.
     */
    public void setObjectHeights (int[] objectHeights)
    {
        _oheights = objectHeights;
    }

    /**
     * Sets the x offset in pixels to the image origin.
     */
    public void setXOrigins (int[] xorigins)
    {
        _xorigins = xorigins;
    }

    /**
     * Sets the y offset in pixels to the image origin.
     */
    public void setYOrigins (int[] yorigins)
    {
        _yorigins = yorigins;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", owidths=").append(StringUtil.toString(_owidths));
	buf.append(", oheights=").append(StringUtil.toString(_oheights));
	buf.append(", xorigins=").append(StringUtil.toString(_xorigins));
	buf.append(", yorigins=").append(StringUtil.toString(_yorigins));
    }

    /**
     * Creates instances of {@link ObjectTile}, which can span more than a
     * single tile's space in a display.
     */
    protected Tile createTile (int tileIndex, Image image)
    {
        // default object dimensions to (1, 1)
        int wid = 1, hei = 1;

        // retrieve object dimensions if known
        if (_owidths != null) {
            wid = _owidths[tileIndex];
            hei = _oheights[tileIndex];
        }

        ObjectTile tile = new ObjectTile(image, wid, hei);

        if (_xorigins != null) {
            tile.setOrigin(_xorigins[tileIndex], _yorigins[tileIndex]);
        }

        return tile;
            
    }

    /** The width (in tile units) of our object tiles. */
    protected int[] _owidths;

    /** The height (in tile units) of our object tiles. */
    protected int[] _oheights;

    /** The x offset in pixels to the origin of the tile images. */
    protected int[] _xorigins;

    /** The y offset in pixels to the origin of the tile images. */
    protected int[] _yorigins;
}
