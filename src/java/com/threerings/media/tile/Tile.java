//
// $Id: Tile.java,v 1.27 2003/01/17 02:30:50 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;

import com.threerings.media.image.Mirage;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile implements Cloneable
{
    /**
     * Constructs a tile with the specified image.
     *
     * @param image our tile image in mirage form (which is optimized for
     * screen rendering).
     */
    public Tile (Mirage image)
    {
        _mirage = image;
    }

    /**
     * Returns the width of this tile.
     */
    public int getWidth ()
    {
        return _mirage.getWidth();
    }

    /**
     * Returns the height of this tile.
     */
    public int getHeight ()
    {
        return _mirage.getHeight();
    }

    /**
     * Returns the estimated memory usage of our underlying tile image.
     */
    public long getEstimatedMemoryUsage ()
    {
        return _mirage.getEstimatedMemoryUsage();
    }

    /**
     * Render the tile image at the specified position in the given
     * graphics context.
     */
    public void paint (Graphics2D gfx, int x, int y)
    {
        _mirage.paint(gfx, x, y);
    }

    /**
     * Returns true if the specified coordinates within this tile contains
     * a non-transparent pixel.
     */
    public boolean hitTest (int x, int y)
    {
        return _mirage.hitTest(x, y);
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
        buf.append(_mirage.getWidth()).append("x");
        buf.append(_mirage.getHeight());
    }

    /** Our tileset image. */
    protected Mirage _mirage;
}
