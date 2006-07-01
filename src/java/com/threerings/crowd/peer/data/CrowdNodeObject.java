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

package com.threerings.crowd.peer.data;

import com.threerings.presents.peer.data.NodeObject;

/**
 * Extends the basic {@link NodeObject} with Crowd bits.
 */
public class CrowdNodeObject extends NodeObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>crowdPeerService</code> field. */
    public static final String CROWD_PEER_SERVICE = "crowdPeerService";
    // AUTO-GENERATED: FIELDS END

    /** Used to coordinate tells between servers. */
    public CrowdPeerMarshaller crowdPeerService;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>crowdPeerService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setCrowdPeerService (CrowdPeerMarshaller value)
    {
        CrowdPeerMarshaller ovalue = this.crowdPeerService;
        requestAttributeChange(
            CROWD_PEER_SERVICE, value, ovalue);
        this.crowdPeerService = value;
    }
    // AUTO-GENERATED: METHODS END
}
