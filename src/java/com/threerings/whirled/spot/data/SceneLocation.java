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

import com.threerings.presents.dobj.DSet;

/**
 * Extends {@link Location} with the data and functionality needed to
 * represent a particular user's location in a scene.
 */
public class SceneLocation extends SimpleStreamableObject
    implements DSet.Entry
{
    /** The oid of the body that occupies this location. */
    public int bodyOid;

    /** The actual location, which is interpreted by the display system. */
    public Location loc;

    /**
     * Creates a scene location with the specified information.
     */
    public SceneLocation (Location loc, int bodyOid)
    {
        this.loc = loc;
        this.bodyOid = bodyOid;
    }

    /**
     * Creates a blank instance suitable for unserialization.
     */
    public SceneLocation ()
    {
    }

    // documentation inherited
    public Comparable getKey ()
    {
        if (_key == null) {
            _key = new Integer(bodyOid);
        }
        return _key;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        // TEMP
        if (other instanceof Location) {
            Thread.dumpStack(); // this will help us find logic errors,
            // as a SceneLocation and a Location shouldn't be compared
        }
        // END: temp

        return (other instanceof SceneLocation) &&
            this.loc.equals(((SceneLocation) other).loc);
    }

    // documentation inherited
    public int hashCode ()
    {
        return loc.hashCode();
    }

    /** Used for {@link #getKey}. */
    protected transient Integer _key;
}
