//
// $Id: Location.java,v 1.1 2001/08/09 21:17:06 shaper Exp $

package com.threerings.miso.scene;

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
    /** The spot index in the scene containing this location. */
    public int spotid;

    /** The location position in full coordinates. */
    public int x, y;

    /** The location orientation. */
    public int orient;

    /**
     * Construct a <code>Location</code> object.
     *
     * @param spotid the spot id.
     * @param x the x-position full coordinate.
     * @param y the y-position full coordinate.
     * @param orient the location orientation.
     */
    public Location (int spotid, int x, int y, int orient)
    {
	this.spotid = spotid;
	this.x = x;
	this.y = y;
	this.orient = orient;
    }

    /**
     * Return a String representation of this object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[spotid=").append(spotid);
	buf.append(", x=").append(x);
	buf.append(", y=").append(y);
        buf.append(", orient=").append(orient);
        return buf.append("]").toString();
    }
}
