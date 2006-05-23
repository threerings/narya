//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.whirled.spot.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Represents an exit to another scene. A body sprite would walk over to a
 * portal's coordinates and then either proceed off of the edge of the
 * display, or open a door and walk through it, or fizzle away in a Star
 * Trekkian transporter style or whatever is appropriate for the game in
 * question. It contains information on the scene to which the body exits
 * when using this portal and the location at which the body sprite should
 * appear in that target scene.
 */
public class Portal extends SimpleStreamableObject
    implements Cloneable
{
    /** This portal's unique identifier. */
    public short portalId;

    /** The location of the portal.
     * This field is present on client and server, it is streamed specially. */
    public Location loc;

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
        return (Location) loc.clone();
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
        return loc.getOpposite();
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
        return (other instanceof Portal) &&
            ((Portal) other).portalId == portalId;
    }

    /**
     * Computes a reasonable hashcode for portal instances.
     */
    public int hashCode ()
    {
        return portalId;
    }
}
