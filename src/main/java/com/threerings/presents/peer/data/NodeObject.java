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

package com.threerings.presents.peer.data;

import javax.annotation.Generated;

import com.google.common.base.Objects;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.io.Streamable;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * Contains information that one node published for all of its peers.
 */
public class NodeObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>nodeName</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String NODE_NAME = "nodeName";

    /** The field name of the <code>bootStamp</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String BOOT_STAMP = "bootStamp";

    /** The field name of the <code>peerService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PEER_SERVICE = "peerService";

    /** The field name of the <code>clients</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CLIENTS = "clients";

    /** The field name of the <code>locks</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String LOCKS = "locks";

    /** The field name of the <code>acquiringLock</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String ACQUIRING_LOCK = "acquiringLock";

    /** The field name of the <code>releasingLock</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String RELEASING_LOCK = "releasingLock";

    /** The field name of the <code>cacheData</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CACHE_DATA = "cacheData";
    // AUTO-GENERATED: FIELDS END

    /** Identifies a locked resource. */
    public static class Lock extends SimpleStreamableObject
        implements Comparable<Lock>, DSet.Entry
    {
        /** The resource type.  Only resources of the same type will have their ids compared. */
        public final String type;

        /** The resource identifier, which can be <code>null</code> for singleton resources. */
        public final Comparable<?> id;

        public Lock (String type, Comparable<?> id)
        {
            this.type = type;
            this.id = id;
        }

        // documentation inherited from interface Comparable
        public int compareTo (Lock olock)
        {
            int v1 = type.compareTo(olock.type);
            if (v1 != 0 || id == null) {
                return v1;
            }
            return DSet.compare(id, olock.id);
        }

        // documentation inherited from interface DSet.Entry
        public Comparable<?> getKey ()
        {
            return this;
        }

        @Override
        public int hashCode ()
        {
            return type.hashCode() + (id == null ? 0 : id.hashCode());
        }

        @Override
        public boolean equals (Object other)
        {
            Lock olock = (Lock)other;
            return type.equals(olock.type) && Objects.equal(id, olock.id);
        }
    }

    /** Used for informing peers of changes to persistent data. */
    public static class CacheData extends SimpleStreamableObject
    {
        /** The cache that should be purged. */
        public final String cache;

        /** The stale data in the cache. */
        public final Streamable data;

        public CacheData (String cache, Streamable data)
        {
            this.cache = cache;
            this.data = data;
        }
    }

    /** The node name of this peer. */
    public String nodeName;

    /** The time that this node's JVM started up. */
    public long bootStamp;

    /** The service used to make requests of the node. */
    public PeerMarshaller peerService;

    /** Contains information on all clients connected to this node. */
    public DSet<ClientInfo> clients = new DSet<ClientInfo>();

    /** The set of locks held by this node. */
    public DSet<Lock> locks = new DSet<Lock>();

    /** Used to broadcast a node's desire to acquire a lock. */
    public Lock acquiringLock;

    /** Used to broadcast a node's desire to release a lock. */
    public Lock releasingLock;

    /** A field we use to broadcast changes to possible cached data. */
    public CacheData cacheData;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>nodeName</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setNodeName (String value)
    {
        String ovalue = this.nodeName;
        requestAttributeChange(
            NODE_NAME, value, ovalue);
        this.nodeName = value;
    }

    /**
     * Requests that the <code>bootStamp</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setBootStamp (long value)
    {
        long ovalue = this.bootStamp;
        requestAttributeChange(
            BOOT_STAMP, Long.valueOf(value), Long.valueOf(ovalue));
        this.bootStamp = value;
    }

    /**
     * Requests that the <code>peerService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPeerService (PeerMarshaller value)
    {
        PeerMarshaller ovalue = this.peerService;
        requestAttributeChange(
            PEER_SERVICE, value, ovalue);
        this.peerService = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>clients</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToClients (ClientInfo elem)
    {
        requestEntryAdd(CLIENTS, clients, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>clients</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromClients (Comparable<?> key)
    {
        requestEntryRemove(CLIENTS, clients, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>clients</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setClients (DSet<ClientInfo> value)
    {
        requestAttributeChange(CLIENTS, value, this.clients);
        DSet<ClientInfo> clone = (value == null) ? null : value.clone();
        this.clients = clone;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>locks</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToLocks (NodeObject.Lock elem)
    {
        requestEntryAdd(LOCKS, locks, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>locks</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromLocks (Comparable<?> key)
    {
        requestEntryRemove(LOCKS, locks, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>locks</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateLocks (NodeObject.Lock elem)
    {
        requestEntryUpdate(LOCKS, locks, elem);
    }

    /**
     * Requests that the <code>locks</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setLocks (DSet<NodeObject.Lock> value)
    {
        requestAttributeChange(LOCKS, value, this.locks);
        DSet<NodeObject.Lock> clone = (value == null) ? null : value.clone();
        this.locks = clone;
    }

    /**
     * Requests that the <code>acquiringLock</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setAcquiringLock (NodeObject.Lock value)
    {
        NodeObject.Lock ovalue = this.acquiringLock;
        requestAttributeChange(
            ACQUIRING_LOCK, value, ovalue);
        this.acquiringLock = value;
    }

    /**
     * Requests that the <code>releasingLock</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setReleasingLock (NodeObject.Lock value)
    {
        NodeObject.Lock ovalue = this.releasingLock;
        requestAttributeChange(
            RELEASING_LOCK, value, ovalue);
        this.releasingLock = value;
    }

    /**
     * Requests that the <code>cacheData</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setCacheData (NodeObject.CacheData value)
    {
        NodeObject.CacheData ovalue = this.cacheData;
        requestAttributeChange(
            CACHE_DATA, value, ovalue);
        this.cacheData = value;
    }
    // AUTO-GENERATED: METHODS END
}
