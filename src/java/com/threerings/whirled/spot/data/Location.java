//
// $Id: Location.java,v 1.10 2004/08/27 02:20:45 mdb Exp $
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

import com.threerings.io.TrackedStreamableObject;
import com.threerings.util.DirectionUtil;

/**
 * Contains information on a scene occupant's position and orientation.
 */
public class Location extends TrackedStreamableObject
    implements Cloneable
{
    /** The user's x position (interpreted by the display system). */
    public int x;

    /** The user's y position (interpreted by the display system). */
    public int y;

    /** The user's orientation (defined by {@link DirectionCodes}). */
    public byte orient;

    /** {@link #toString} helper function. */
    public String orientToString ()
    {
        return DirectionUtil.toShortString(orient);
    }

    /**
     * A zero-argument constructor used when unserializing instances.
     */
    public Location ()
    {
    }

    /**
     * Constructs a location with the specified coordinates and
     * orientation.
     */
    public Location (int x, int y, byte orient)
    {
        this.x = x;
        this.y = y;
        this.orient = orient;

        if (orient == -1) {
            Thread.dumpStack();
        }
    }

    /**
     * Creates a clone of this instance.
     */
    public Object clone ()
    {
        try {
            return (Location)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Location.clone() failed " + cnse);
        }
    }

    /**
     * Location equality is determined by coordinates.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Location) {
            Location oloc = (Location)other;
            return (x == oloc.x) && (y == oloc.y);
        } else {
            return false;
        }
    }

    /**
     * Location equivalence means that the coordinates and orientation are
     * the same.
     */
    public boolean equivalent (Location oloc)
    {
        return equals(oloc) && (orient == oloc.orient);
    }

    /**
     * Computes a reasonable hashcode for location instances.
     */
    public int hashCode ()
    {
        return x ^ y;
    }
}
