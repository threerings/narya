//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.data;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * Contains information that one node published for all of its peers.
 */
public class NodeObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>clients</code> field. */
    public static final String CLIENTS = "clients";
    // AUTO-GENERATED: FIELDS END

    /** Contains information on all clients connected to this node. */
    public DSet<ClientInfo> clients;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>clients</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToClients (ClientInfo elem)
    {
        requestEntryAdd(CLIENTS, clients, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>clients</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromClients (Comparable key)
    {
        requestEntryRemove(CLIENTS, clients, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>clients</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateClients (ClientInfo elem)
    {
        requestEntryUpdate(CLIENTS, clients, elem);
    }

    /**
     * Requests that the <code>clients</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setClients (DSet<com.threerings.presents.peer.data.ClientInfo> value)
    {
        requestAttributeChange(CLIENTS, value, this.clients);
        this.clients = (value == null) ? null : value.typedClone();
    }
    // AUTO-GENERATED: METHODS END
}
