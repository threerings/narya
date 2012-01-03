//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import javax.annotation.Generated;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.peer.data.NodeObject;

import com.threerings.crowd.chat.data.ChatChannel;

/**
 * Extends the basic {@link NodeObject} with Crowd bits.
 */
public class CrowdNodeObject extends NodeObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>crowdPeerService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CROWD_PEER_SERVICE = "crowdPeerService";

    /** The field name of the <code>hostedChannels</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String HOSTED_CHANNELS = "hostedChannels";
    // AUTO-GENERATED: FIELDS END

    /** Used to coordinate tells between servers. */
    public CrowdPeerMarshaller crowdPeerService;

    /** The chat channels hosted on this server. */
    public DSet<ChatChannel> hostedChannels = new DSet<ChatChannel>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>crowdPeerService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setCrowdPeerService (CrowdPeerMarshaller value)
    {
        CrowdPeerMarshaller ovalue = this.crowdPeerService;
        requestAttributeChange(
            CROWD_PEER_SERVICE, value, ovalue);
        this.crowdPeerService = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>hostedChannels</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToHostedChannels (ChatChannel elem)
    {
        requestEntryAdd(HOSTED_CHANNELS, hostedChannels, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>hostedChannels</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromHostedChannels (Comparable<?> key)
    {
        requestEntryRemove(HOSTED_CHANNELS, hostedChannels, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>hostedChannels</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateHostedChannels (ChatChannel elem)
    {
        requestEntryUpdate(HOSTED_CHANNELS, hostedChannels, elem);
    }

    /**
     * Requests that the <code>hostedChannels</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setHostedChannels (DSet<ChatChannel> value)
    {
        requestAttributeChange(HOSTED_CHANNELS, value, this.hostedChannels);
        DSet<ChatChannel> clone = (value == null) ? null : value.clone();
        this.hostedChannels = clone;
    }
    // AUTO-GENERATED: METHODS END
}
