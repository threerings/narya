//
// $Id: TrimmedTile.java,v 1.3 2002/06/19 23:28:14 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.image.BufferedImage;

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
    public void paint (Graphics gfx, int x, int y)
    {
        if (_subimage == null) {
            createSubImage();
        }
        gfx.drawImage(_subimage, x + _tbounds.x, y + _tbounds.y, null);
    }

    /**
     * Returns the bounds of the trimmed image within the coordinate
     * system defined by the complete virtual tile. The returned rectangle
     * should <em>not</em> be modified.
     */
    public Rectangle getTrimmedBounds ()
    {
        return _tbounds;
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
    protected void createSubImage ()
    {
        if (_image instanceof BufferedImage) {
            _subimage = ImageUtil.getSubimage(_image, _bounds.x, _bounds.y,
                                              _tbounds.width, _tbounds.height);
        } else {
            String errmsg = "Can't obtain tile image [tile=" + this + "].";
            throw new RuntimeException(errmsg);
        }
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
