//
// $Id: ClusterObject.java 3288 2004-12-28 03:51:29Z mdb $
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

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

import com.threerings.crowd.chat.data.SpeakObject;

/**
 * Used to dispatch chat in clusters.
 */
public class ClusterObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>occupants</code> field. */
    public static const OCCUPANTS :String = "occupants";
    // AUTO-GENERATED: FIELDS END

    /**
     * Tracks the oid of the body objects that occupy this cluster.
     */
    public var occupants :OidList = new OidList();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that <code>oid</code> be added to the <code>occupants</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    public function addToOccupants (oid :int) :void
    {
        requestOidAdd(OCCUPANTS, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public function removeFromOccupants (oid :int) :void
    {
        requestOidRemove(OCCUPANTS, oid);
    }
    // AUTO-GENERATED: METHODS END
}
}
