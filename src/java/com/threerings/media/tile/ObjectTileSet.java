//
// $Id: ObjectTileSet.java,v 1.12 2003/01/29 21:53:51 mdb Exp $

package com.threerings.media.tile;

import com.samskivert.util.StringUtil;

import com.threerings.media.image.Mirage;

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

    /**
     * Sets the default render priorities for our object tiles.
     */
    public void setPriorities (int[] priorities)
    {
        _priorities = priorities;
    }

    /**
     * Provides a set of colorization classes that apply to objects in
     * this tileset.
     */
    public void setColorizations (String[] zations)
    {
        _zations = zations;
    }

    /**
     * Returns the colorization classes that should be used to recolor
     * objects in this tileset.
     */
    public String[] getColorizations ()
    {
        return _zations;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", owidths=").append(StringUtil.toString(_owidths));
	buf.append(", oheights=").append(StringUtil.toString(_oheights));
	buf.append(", xorigins=").append(StringUtil.toString(_xorigins));
	buf.append(", yorigins=").append(StringUtil.toString(_yorigins));
	buf.append(", prios=").append(StringUtil.toString(_priorities));
	buf.append(", zations=").append(StringUtil.toString(_zations));
    }

    /**
     * Creates instances of {@link ObjectTile}, which can span more than a
     * single tile's space in a display.
     */
    protected Tile createTile (int tileIndex, Mirage image)
    {
        ObjectTile tile = new ObjectTile(image);
        if (_owidths != null) {
            tile.setBase(_owidths[tileIndex], _oheights[tileIndex]);
        }
        if (_xorigins != null) {
            tile.setOrigin(_xorigins[tileIndex], _yorigins[tileIndex]);
        }
        if (_priorities != null) {
            tile.setPriority(_priorities[tileIndex]);
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

    /** The default render priorities of our objects. */
    protected int[] _priorities;

    /** Colorization classes that apply to our objects. */
    protected String[] _zations;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
