//
// $Id: Location.java,v 1.3 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.spot.data;

/**
 * A location represents a place to stand in a scene. More specifically,
 * it is a geometric location in a scene with an orientation that would be
 * assumed by any body sprite standing in that location. Locations may
 * optionally belong to a cluster, which associates them with a group of
 * locations for speaking purposes (bodies in the same cluster can "hear"
 * one another speak).
 */
public class Location
{
    /** This location's unique identifier. */
    public int locationId;

    /** This location's x coordinate (interpreted by the display system). */
    public int x;

    /** This location's y coordinate (interpreted by the display system). */
    public int y;

    /** This location's y orientation (interpreted by the display system). */
    public int orientation;

    /** The cluster to which this location belongs or -1 if it belongs to
     * no cluster. */
    public int clusterIndex;

    /**
     * Location equality is determined by location id.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Location) {
            Location oloc = (Location)other;
            return locationId == oloc.locationId;
        } else {
            return false;
        }
    }

    /**
     * Computes a reasonable hashcode for location instances.
     */
    public int hashCode ()
    {
        return locationId;
    }

    /**
     * Generates a string representation of this location instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An efficient, extensible mechanism for generating a string
     * representation of locations and derived classes.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("id=").append(locationId);
        buf.append(", x=").append(x);
        buf.append(", y=").append(y);
        buf.append(", orient=").append(orientation);
        buf.append(", cluster=").append(clusterIndex);
    }
}
