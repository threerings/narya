//
// $Id: TrimmedTile.java,v 1.1 2002/05/06 18:08:32 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

import com.samskivert.util.StringUtil;

import com.threerings.media.util.ImageUtil;

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
     * @param bounds contains the width and height of the
     * <em>untrimmed</em> tile, but the x and y offset of the
     * <em>trimmed</em> tile image in the supplied tileset source image.
     * @param tbounds the bounds of the trimmed image in the coordinate
     * system defined by the untrimmed image.
     */
    public TrimmedTile (Image image, Rectangle bounds, Rectangle tbounds)
    {
        super(image, bounds);
        _tbounds = tbounds;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, int x, int y)
    {
        int vx = x + _tbounds.x, vy = y + _tbounds.y;
        Shape oclip = gfx.getClip();
        gfx.clipRect(vx, vy, _tbounds.width, _tbounds.height);
	gfx.drawImage(_image, vx - _bounds.x, vy - _bounds.y, null);
        gfx.setClip(oclip);
    }

    // documentation inherited
    public Image getImage ()
    {
        String errmsg = "Can't convert trimmed tile to image " +
            "[tile=" + this + "].";
        throw new RuntimeException(errmsg);
    }

    // documentation inherited
    public boolean hitTest (int x, int y)
    {
        return ImageUtil.hitTest(_image, _bounds.x + x, _bounds.y + y);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
	buf.append(", tbounds=").append(StringUtil.toString(_tbounds));
    }

    /** The dimensions of the trimmed image in the coordinate space
     * defined by the untrimmed image. */
    protected Rectangle _tbounds;
}
