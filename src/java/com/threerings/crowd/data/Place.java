//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Contains information on the current place occupied by a body.
 */
public class Place extends SimpleStreamableObject
{
    /** The oid of this place's {@link PlaceObject}. */
    public int placeOid;

    /** Used when unserializing. */
    public Place ()
    {
    }

    /**
     * Creates a place with the supplied oid.
     */
    public Place (int placeOid)
    {
        this.placeOid = placeOid;
    }

    @Override // from Object
    public boolean equals (Object other)
    {
        if (other == null) {
            return false;
        }
        return getClass().equals(other.getClass()) ? placeOid == ((Place)other).placeOid : false;
    }

    @Override // from Object
    public int hashCode ()
    {
        return placeOid;
    }
}
