//
// $Id: Tile.java,v 1.23 2002/09/13 20:27:05 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.util.ImageUtil;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile implements Cloneable
{
    /**
     * Constructs a tile with the specified tileset image.
     *
     * @param image the tileset image from which our tile image is
     * extracted.
     * @param bounds the bounds of the tile image within the greater
     * tileset image.
     */
    public Tile (Image image, Rectangle bounds)
    {
        _image = image;
        _bounds = bounds;
    }

    /**
     * Called to create our subimage.
     */
    protected void createSubImage ()
    {
        if (_image instanceof BufferedImage) {
            try {
                _subimage = ImageUtil.getSubimage(
                    _image, _bounds.x, _bounds.y,
                    _bounds.width, _bounds.height);
            } catch (RuntimeException rte) {
                Log.warning("Failure creating subimage [src=" + _image +
                            ", bounds=" + StringUtil.toString(_bounds) +
                            ", error=" + rte + "].");
                throw rte;
            }

        } else {
            String errmsg = "Can't obtain tile image [tile=" + this + "].";
            throw new RuntimeException(errmsg);
        }
    }

    /**
     * Returns the width of this tile.
     */
    public int getWidth ()
    {
        return _bounds.width;
    }

    /**
     * Returns the height of this tile.
     */
    public int getHeight ()
    {
        return _bounds.height;
    }

    /**
     * Render the tile image at the specified position in the given
     * graphics context.
     */
    public void paint (Graphics gfx, int x, int y)
    {
        gfx.drawImage(getImage(), x, y, null);
    }

    /**
     * Returns true if the specified coordinates within this tile contains
     * a non-transparent pixel.
     */
    public boolean hitTest (int x, int y)
    {
        return ImageUtil.hitTest(_image, _bounds.x + x, _bounds.y + y);
    }

    /**
     * Returns the image used to render this tile, if access to that image
     * can be obtained. The tile must have been created with a {@link
     * BufferedImage} and derived classes may refuse to implement this
     * function if they do special stuff that hampers their ability to
     * return a single image describing the tile.
     */
    public Image getImage ()
    {
        if (_subimage == null) {
            createSubImage();
        }
        return _subimage;
    }

    /**
     * Creates a shallow copy of this tile object.
     */
    public Object clone ()
    {
        try {
            return (Tile)super.clone();
        } catch (CloneNotSupportedException cnse) {
            String errmsg = "All is wrong with the universe: " + cnse;
            throw new RuntimeException(errmsg);
        }
    }

    /**
     * Return a string representation of this tile.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific tile information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
	buf.append("image=").append(_image);
	buf.append(", bounds=").append(StringUtil.toString(_bounds));
    }

    /** Our tileset image. */
    protected Image _image;

    /** Our cropped tileset image. */
    protected Image _subimage;

    /** The bounds of the tile image within the tileset image. */
    protected Rectangle _bounds;
}
