//
// $Id: Portal.java,v 1.1 2001/11/13 02:25:35 mdb Exp $

package com.threerings.whirled.spot.data;

/**
 * A portal is a location, but one that represents an exit to another
 * scene rather than a place to stand in the current scene. A body sprite
 * would walk over to a portal's coordinates and then either proceed off
 * of the edge of the display, or open a door and walk through it, or
 * fizzle away in a Star Trekkian transporter style or whatever is
 * appropriate for the game in question. It contains information on the
 * scene to which the body exits when using this portal and the location
 * at which the body sprite should appear in that target scene.
 *
 * <p> Though portals extend locations, they generally would not occupy a
 * cluster (because that wouldn't make much sense) and as such should
 * always have a {@link #clusterIndex} of -1.
 */
public class Portal extends Location
{
    /** The scene identifier of the scene to which a body will exit when
     * they "use" this portal. */
    public int targetSceneId;

    /** The location identifier of the location at which a body will enter
     * the target scene when they "use" this portal. */
    public int targetLocationId;

    /**
     * Constructs a portal with the supplied values.
     */
    public Portal (int id, int x, int y, int orientation,
                   int targetSceneId, int targetLocationId)
    {
        super(id, x, y, orientation, -1);
        this.targetSceneId = targetSceneId;
        this.targetLocationId = targetLocationId;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", targetScene=").append(targetSceneId);
        buf.append(", targetLoc=").append(targetLocationId);
    }
}
