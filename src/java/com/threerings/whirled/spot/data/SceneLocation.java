//
// $Id: SceneLocation.java,v 1.2 2004/08/27 02:20:45 mdb Exp $
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

import com.threerings.presents.dobj.DSet;

/**
 * Extends {@link Location} with the data and functionality needed to
 * represent a particular user's location in a scene.
 */
public class SceneLocation extends Location
    implements DSet.Entry
{
    /** The oid of the body that occupies this location. */
    public int bodyOid;

    /**
     * Creates a scene location with the specified information.
     */
    public SceneLocation (int x, int y, byte orient, int bodyOid)
    {
        super(x, y, orient);
        this.bodyOid = bodyOid;
    }

    /**
     * Creates a scene location with the specified information.
     */
    public SceneLocation (Location loc, int bodyOid)
    {
        super(loc.x, loc.y, loc.orient);
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

    /** Used for {@link #geyKey}. */
    protected transient Integer _key;
}
