//
// $Id: Location.java,v 1.8 2001/10/25 16:36:42 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.threerings.media.sprite.Sprite;

/**
 * A location object represents a unique well-defined location within
 * the scene at the lowest level of granularity available within the
 * scene coordinate system.  Locations reside at a full coordinate
 * (comprised of tile coordinates and fine coordinates within the
 * tile), and only one location may reside at each full coordinate in
 * the scene.
 */
public class Location
{
    /** The unique identifier for this location. */
    public int id = -1;

    /** The location position in full coordinates. */
    public int x, y;

    /** The location orientation. */
    public int orient;

    /**
     * Constructs a location object.
     *
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     * @param orient the location orientation.
     */
    public Location (int x, int y, int orient)
    {
	this.x = x;
	this.y = y;
	this.orient = orient;
    }

    /**
     * Constructs a location object with a default orientation.
     *
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     */
    public Location (int x, int y)
    {
	this.x = x;
	this.y = y;
	this.orient = Sprite.DIR_SOUTHWEST;
    }

    /**
     * Renders the location centered at the given coordinates to the
     * given graphics context.
     *
     * @param gfx the graphics context.
     * @param cx the center x-coordinate.
     * @param cy the center y-coordinate.
     */
    public void paint (Graphics2D gfx, int cx, int cy)
    {
        // translate the origin to center on the location
        gfx.translate(cx, cy);

        // rotate to reflect the location orientation
        double rot = (Math.PI / 4.0f) * orient;
        gfx.rotate(rot);

        // draw the triangle
        gfx.setColor(getColor());
        gfx.fill(_tri);

        // outline the triangle in black
        gfx.setColor(Color.black);
        gfx.draw(_tri);

        // draw the rectangle
        gfx.setColor(Color.red);
        gfx.fillRect(-1, 2, 3, 3);

        // restore the original transform
        gfx.rotate(-rot);
        gfx.translate(-cx, -cy);
    }

    /**
     * Returns a string representation of the location.
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
     * specific event information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
	buf.append("id=").append(id);
	buf.append(", x=").append(x);
	buf.append(", y=").append(y);
        buf.append(", orient=").append(orient);
    }

    /**
     * Returns the color to paint the inside of the location.
     */
    protected Color getColor ()
    {
        return Color.yellow;
    }

    /** The triangle used to render a location on-screen. */
    protected static Polygon _tri;

    static {
        _tri = new Polygon();
        _tri.addPoint(-3, -3);
        _tri.addPoint(3, -3);
        _tri.addPoint(0, 3);
    };
}
