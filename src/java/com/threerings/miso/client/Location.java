//
// $Id: Location.java,v 1.4 2001/08/11 00:00:13 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.sprite.Path;

/**
 * The <code>Location</code> class represents a unique well-defined
 * location within the scene at the lowest level of granularity
 * available within the scene coordinate system.  Locations reside at
 * a full coordinate (comprised of tile coordinates and fine
 * coordinates within the tile), and only one location may reside at
 * each full coordinate in the scene. 
 */
public class Location
{
    /** The location position in full coordinates. */
    public int x, y;

    /** The location orientation. */
    public int orient;

    /**
     * Construct a <code>Location</code> object.
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
     * Construct a <code>Location</code> object with a default orientation.
     *
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     */
    public Location (int x, int y)
    {
	this.x = x;
	this.y = y;
	this.orient = Path.DIR_SOUTHWEST;
    }

    /**
     * Return a String representation of this object.
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
	buf.append("x=").append(x);
	buf.append(", y=").append(y);
        buf.append(", orient=").append(orient);
    }
}
