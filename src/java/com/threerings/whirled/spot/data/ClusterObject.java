//
// $Id: ClusterObject.java,v 1.4 2004/08/27 02:20:45 mdb Exp $
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

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

import com.threerings.crowd.chat.data.SpeakObject;

/**
 * Used to dispatch chat in clusters.
 */
public class ClusterObject extends DObject
    implements SpeakObject
{
    /** The field name of the <code>occupants</code> field. */
    public static final String OCCUPANTS = "occupants";

    /**
     * Tracks the oid of the body objects that occupy this cluster.
     */
    public OidList occupants = new OidList();

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        for (int ii = 0, ll = occupants.size(); ii < ll; ii++) {
            op.apply(occupants.get(ii));
        }
    }

    /**
     * Requests that the specified oid be added to the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, oid);
    }

    /**
     * Requests that the specified oid be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, oid);
    }
}
