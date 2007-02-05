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

import com.threerings.io.Streamable;

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

    /** The field name of the <code>locks</code> field. */
    public static final String LOCKS = "locks";

    /** The field name of the <code>acquiringLock</code> field. */
    public static final String ACQUIRING_LOCK = "acquiringLock";

    /** The field name of the <code>releasingLock</code> field. */
    public static final String RELEASING_LOCK = "releasingLock";

    /** The field name of the <code>cacheData</code> field. */
    public static final String CACHE_DATA = "cacheData";
    // AUTO-GENERATED: FIELDS END

    /** Used for informing peers of changes to persistent data. */
    public static class CacheData implements Streamable
    {
        /** The cache that should be purged. */
        public String cache;

        /** The stale data in the cache. */
        public Streamable data;

        public CacheData () 
        { 
        }

        public CacheData (String cache, Streamable data)
        {
            this.cache = cache;
            this.data = data;
        }
    }

    /** Contains information on all clients connected to this node. */
    public DSet<ClientInfo> clients = new DSet<ClientInfo>();

    /** This node's view of the peer lock set.  A lock is acquired only when it has been added to
     * all peers' sets, and is released only when it has been removed from all peers' sets. */
    public DSet<Lock> locks = new DSet<Lock>();

    /** Used to broadcast a node's desire to acquire a lock. */
    public Lock.Name acquiringLock;
    
    /** Used to broadcast a node's desire to release a lock. */
    public Lock.Name releasingLock;
    
    /** A field we use to broadcast changes to possible cached data. */
    public CacheData cacheData;

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
        @SuppressWarnings("unchecked") DSet<com.threerings.presents.peer.data.ClientInfo> clone =
            (value == null) ? null : value.typedClone();
        this.clients = clone;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>locks</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToLocks (Lock elem)
    {
        requestEntryAdd(LOCKS, locks, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>locks</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromLocks (Comparable key)
    {
        requestEntryRemove(LOCKS, locks, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>locks</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateLocks (Lock elem)
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
    public void setLocks (DSet<com.threerings.presents.peer.data.Lock> value)
    {
        requestAttributeChange(LOCKS, value, this.locks);
        @SuppressWarnings("unchecked") DSet<com.threerings.presents.peer.data.Lock> clone =
            (value == null) ? null : value.typedClone();
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
    public void setAcquiringLock (Lock.Name value)
    {
        Lock.Name ovalue = this.acquiringLock;
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
    public void setReleasingLock (Lock.Name value)
    {
        Lock.Name ovalue = this.releasingLock;
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
    public void setCacheData (NodeObject.CacheData value)
    {
        NodeObject.CacheData ovalue = this.cacheData;
        requestAttributeChange(
            CACHE_DATA, value, ovalue);
        this.cacheData = value;
    }
    // AUTO-GENERATED: METHODS END
}
