//
// $Id: Location.java 3726 2005-10-11 19:17:43Z ray $
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

import com.threerings.io.Streamable;

import com.threerings.util.Hashable;

/**
 * Contains information on a scene occupant's position and orientation.
 */
public interface Location extends Streamable, Hashable
{
    /**
     * Get a new Location instance that is equals() to this one but that
     * has an orientation facing the opposite direction.
     */
    function getOpposite () :Location;

    /**
     * Two locations are equivalent if they specify the same location
     * and orientation.
     */
    function equivalent (other :Location) :Boolean;

    /**
     * Locations are cloneable.
     */
    function clone () :Object;
}
}
