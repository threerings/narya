//
// $Id: Portal.java,v 1.6 2004/08/23 21:05:04 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.io.TrackedStreamableObject;
import com.threerings.util.DirectionUtil;

/**
 * Represents an exit to another scene. A body sprite would walk over to a
 * portal's coordinates and then either proceed off of the edge of the
 * display, or open a door and walk through it, or fizzle away in a Star
 * Trekkian transporter style or whatever is appropriate for the game in
 * question. It contains information on the scene to which the body exits
 * when using this portal and the location at which the body sprite should
 * appear in that target scene.
 */
public class Portal extends TrackedStreamableObject
    implements Cloneable
{
    /** This portal's unique identifier. */
    public short portalId;

    /** This portal's x coordinate (interpreted by the display system). */
    public int x;

    /** This portal's y coordinate (interpreted by the display system). */
    public int y;

    /** This portal's y orientation (interpreted by the display system). */
    public byte orient;

    /** The scene identifier of the scene to which a body will exit when
     * they "use" this portal. */
    public int targetSceneId;

    /** The portal identifier of the portal at which a body will enter
     * the target scene when they "use" this portal. */
    public short targetPortalId;

    /**
     * Returns a location instance configured with the location and
     * orientation of this portal.
     */
    public Location getLocation ()
    {
        return new Location(x, y, orient);
    }

    /**
     * Returns a location instance configured with the location and
     * opposite orientation of this portal. This is useful for when a body
     * is entering a scene at a portal and we want them to face the
     * opposite direction (as they are entering via the portal rather than
     * leaving, which is the natural "orientation" of a portal).
     */
    public Location getOppLocation ()
    {
        return new Location(x, y, (byte)DirectionUtil.getOpposite(orient));
    }

    /**
     * Returns true if the portal has a potentially valid target scene and
     * portal id (they are not guaranteed to exist, but they are at least
     * potentially valid values rather than -1 or 0).
     */
    public boolean isValid ()
    {
        return (targetSceneId > 0) && (targetPortalId > 0);
    }

    /**
     * Creates a clone of this instance.
     */
    public Object clone ()
    {
        try {
            return (Portal)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Portal.clone() failed " + cnse);
        }
    }

    /**
     * Portal equality is determined by portal id.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Portal) {
            Portal oport = (Portal)other;
            return portalId == oport.portalId;
        } else {
            return false;
        }
    }

    /** {@link #toString} helper function. */
    public String orientToString ()
    {
        return DirectionUtil.toShortString(orient);
    }

    /**
     * Computes a reasonable hashcode for portal instances.
     */
    public int hashCode ()
    {
        return portalId;
    }
}
