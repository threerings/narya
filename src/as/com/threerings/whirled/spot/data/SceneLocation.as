//
// $Id: SceneLocation.java 3310 2005-01-24 23:08:21Z mdb $
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

package com.threerings.whirled.spot.data {

import com.threerings.util.Hashable;

import com.threerings.presents.dobj.DSet_Entry;

/**
 * Extends {@link Location} with the data and functionality needed to
 * represent a particular user's location in a scene.
 */
public class SceneLocation
    implements DSet_Entry, Hashable
{
    /** The oid of the body that occupies this location. */
    public var bodyOid :int;

    /** The actual location, which is interpreted by the display system. */
    public var loc :Location;

    /**
     * Creates a scene location with the specified information.
     */
    public function SceneLocation (loc :Location = null, bodyOid :int = 0)
    {
        this.loc = loc;
        this.bodyOid = bodyOid;
    }

    // documentation inherited from interface DSet_Entry
    public function getKey () :Object
    {
        return bodyOid;
    }

    // documentation inherited from interface Hashable
    public function equals (other :Object) :Boolean
    {
        return (other is SceneLocation) &&
            this.loc.equals((other as SceneLocation).loc);
    }

    // documentation inherited from interface Hashable
    public function hashCode () :int
    {
        return loc.hashCode();
    }
}
}
