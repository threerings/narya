//
// $Id: TrimmedTile.java,v 1.5 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

import com.threerings.media.image.Mirage;

/**
 * Behaves just like a regular tile, but contains a "trimmed" image which
 * is one where the source image has been trimmed to the smallest
 * rectangle that contains all the non-transparent pixels of the original
 * image.
 */
public class TrimmedTile extends Tile
{
    /**
     * Creates a trimmed tile using the supplied tileset image with the
     * specified bounds and coordinates.
     *
     * @param tilesetSource the tileset image that contains our trimmed
     * tile image.
     * @param tbounds contains the width and height of the
     * <em>untrimmed</em> tile, but the x and y offset of the
     * <em>trimmed</em> tile image in the original untrimmed tile image.
     */
    public TrimmedTile (Mirage image, Rectangle tbounds)
    {
        super(image);
        _tbounds = tbounds;
    }

    // documentation inherited
    public int getWidth ()
    {
        return _tbounds.width;
    }

    // documentation inherited
    public int getHeight ()
    {
        return _tbounds.height;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, int x, int y)
    {
        _mirage.paint(gfx, x + _tbounds.x, y + _tbounds.y);
    }

    /**
     * Fills in the bounds of the trimmed image within the coordinate
     * system defined by the complete virtual tile.
     */
    public void getTrimmedBounds (Rectangle tbounds)
    {
        tbounds.setBounds(_tbounds.x, _tbounds.y,
                          _mirage.getWidth(), _mirage.getHeight());
    }

    // documentation inherited
    public boolean hitTest (int x, int y)
    {
        return super.hitTest(x - _tbounds.x, y - _tbounds.y);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
	buf.append(", tbounds=").append(StringUtil.toString(_tbounds));
    }

    /** Our extra trimmed image dimension information. */
    protected Rectangle _tbounds;
}
