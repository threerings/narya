//
// $Id: Tile.java,v 1.18 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Rectangle;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile
{
    /**
     * Constructs a tile with the specified image.
     */
    public Tile (Image image)
    {
        _image = image;
    }

    /**
     * Returns the width of this tile.
     */
    public int getWidth ()
    {
        return _image.getWidth(null);
    }

    /**
     * Returns the height of this tile.
     */
    public int getHeight ()
    {
        return _image.getHeight(null);
    }

    /**
     * Returns this tile's image.
     */
    public Image getImage ()
    {
        return _image;
    }

    /**
     * Render the tile image at the top-left corner of the given shape in
     * the given graphics context.
     */
    public void paint (Graphics2D gfx, Shape dest)
    {
	Rectangle bounds = dest.getBounds();
	gfx.drawImage(_image, bounds.x, bounds.y, null);
    }

    /**
     * Return a string representation of this tile.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer();
        buf.append("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific tile information to the string buffer.
     */
    public void toString (StringBuffer buf)
    {
	buf.append("image=").append(_image);
    }

    /** Our tile image. */
    protected Image _image;
}
