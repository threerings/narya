//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Interval;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.Tuple;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.client.Mapping;
import com.threerings.presents.peer.client.RemoteMapping;
import com.threerings.presents.peer.data.MappingMarshaller;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsDObjectMgr;

import static com.threerings.presents.Log.log;

/**
 * Maintains key/value mappings that are shared among all peers in a distributed system. To avoid
 * negative performance repercussions when scaling to very large peer networks, a variety of
 * replication policies are supported for mappings. These are defined in {@link
 * MappingManager.ReplicationPolicy}.
 *
 * <p>Mappings come in two main forms: published mappings, for which changes are broadcast to all
 * peers so that every peer has a copy of the mapping data residing locally in memory, which is
 * available for immediate access; and remote mappings, for which changes are not broadcast, and
 * peers must make a request to a mapping server when they wish to fetch a mapping's current value,
 * or they may ship a unit of code over to the mapping server which can make local access to the
 * mapping and either communicate an aggregated response, or forward some action or activity to yet
 * another server as a result of the mapped values.</p>
 *
 * <p>The mapping subsystem selects a subset of the peers in the full network to act as primary
 * mapping servers, or ministers. Sufficiently many peers are selected to allow for fault tolerance
 * in the event of peer failure, but sufficiently few to avoid undue overhead of copying data
 * around the network. The precise subset of peers that are turned into ministers is configurable
 * via {@link #TODO}.</p>
 *
 * <p>Mapping configuration must be accomplished during the server initialization period,
 * <em>before</em> the server begins operation. This means that managers which require mappings
 * should inject the {@link MappingManager} into their constructor and {@link #configure} their
 * mappings at construct time.</p>
 */
@Singleton
public class MappingManager
    implements MappingProvider, Lifecycle.ShutdownComponent
{
    /** Defines our replication policies. See the static factory methods (e.g. {@link
     * #immediateRebroadcast} for documentation on the individual policies. */
    public static abstract class ReplicationPolicy
    {
        /** Defines a {@link Mapping} whereby changes to the mapping are immediately broadcast to
         * all peers. This ensures that peers always have the most up-to-date information, at the
         * expense of high network traffic if the information is updated frequently. */
        public static ReplicationPolicy immediateRebroadcast () {
            return new ImmediateRebroadcast();
        }

        /** Defines a {@link Mapping} whereby changes to the mapping are batched up and broadcast
         * to peers every {@code batchMillis} milliseconds. This saves the overhead of numerous
         * invidual communications between peers, at the expense that the information available on
         * each peer may be slightly out of date. */
        public static ReplicationPolicy batchedRebroadcast (int batchMillis) {
            return new BatchedRebroadcast(batchMillis);
        }

        /** Defines a {@link RemoteMapping} whereby changes to the mapping are never broadcast to
         * all peers. Instead peers must request the current value of a mapping via {@link
         * RemoteMapping#get} or package up a unit of work to be executed on a mapping server via
         * {@link RemoteMapping#with}.
         */
        public static ReplicationPolicy noRebroadcast () {
            return new NoRebroadcast();
        }

        /** Returns true if this is a remote mapping. */
        public abstract boolean isRemote ();

        /** Returns true if pending updates should be broadcast, given the specified number of
         * milliseconds elapsed since their last broadcast. */
        public abstract boolean shouldBroadcast (long elapsedMillis);

        /*package*/ ReplicationPolicy () {} // no extending outside this package
    }

    @Inject public MappingManager (Lifecycle cycle, PresentsDObjectMgr omgr)
    {
        cycle.addComponent(this);
        // create our flush interval, which runs every BROADCAST_INTERVAL milliseconds
        (_flusher = new Interval(omgr) {
            public void expired () {
                broadcastUpdates();
            }
        }).schedule(BROADCAST_INTERVAL, true);
    }

    /**
     * Configures the mapping with the specified id. It is assumed that all peers will make the
     * same sequence of {@link #configure} calls during server initialization, and thusly all
     * mapping servers will have the appropriate configuration before mappings are used. Attempts
     * to use a mapping before it has been configured will fail.
     */
    public void configure (Mapping.Id<?,?> id, ReplicationPolicy policy)
    {
        Preconditions.checkArgument(!_configs.containsKey(id.id),
                                    "Mapping " + id + " has already been configured.");
        MappingConfig config = new MappingConfig(id.id, policy);
        _configs.put(id.id, config);
        _mappings.put(config.mapIdx, new LocalMapping<Object,Object>(config));
    }

    /**
     * Returns the non-remote {@link Mapping} for the specified id.
     *
     * @throws IllegalArgumentException if the mapping in question is not a local mapping or does
     * not exist (or has not yet been configured, which is effectively the same thing).
     */
    public <K,V> Mapping<K,V> getMapping (Mapping.Id<K,V> id)
    {
        MappingConfig config = requireConfig(id.id);
        Preconditions.checkArgument(!config.repl.isRemote(), id + " is a remote mapping.");
        return this.<K,V>getMapping(config.mapIdx);
    }

    /**
     * Returns the {@link RemoteMapping} for the specified id.
     *
     * @throws IllegalArgumentException if the mapping in question is not a remote mapping or does
     * not exist (or has not yet been configured, which is effectively the same thing).
     */
    public <K,V> RemoteMapping<K,V> getRemoteMapping (RemoteMapping.Id<K,V> id)
    {
        MappingConfig config = requireConfig(id.id);
        Preconditions.checkArgument(config.repl.isRemote(), id + " is not a remote mapping.");
        return new ProxiedMapping<K,V>(config);
    }

    // TODO: access control checks for all these?

    // from interface MappingProvider
    public void get (ClientObject caller, short mapIdx, Object key,
                     InvocationService.ResultListener rl)
        throws InvocationException
    {
        Preconditions.checkState(
            _isMinister, "Rejecting get() on non-minister [midx=%s, key=%s]", mapIdx, key);
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to get from unknown mapping [idx=%s, key=%s]", mapIdx, key);
        rl.requestProcessed(m.get(key));
    }

    // from interface MappingProvider
    public void put (ClientObject caller, short mapIdx, Object key, Object value)
    {
        Preconditions.checkState(_isMinister, "Rejecting put() on non-minister " +
                                 "[midx=%s, key=%s, value=%s]", mapIdx, key, value);
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to put into unknown mapping [idx=%s, key=%s]", mapIdx, key);
        m.put(key, value);
    }

    // from interface MappingProvider
    public void remove (ClientObject caller, short mapIdx, Object key)
    {
        Preconditions.checkState(
            _isMinister, "Rejecting remove() on non-minister [midx=%s, key=%s]", mapIdx, key);
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to remove from unknown mapping [idx=%s, key=%s]", mapIdx, key);
        m.remove(key);
    }

    // from interface MappingProvider
    public <K,V> void withValue (ClientObject caller, short mapIdx, K key,
                                 RemoteMapping.Action<V> action)
    {
        Preconditions.checkState(
            _isMinister, "Rejecting withValue() on non-minister [midx=%s, key=%s]", mapIdx, key);
        Mapping<K,V> mapping = this.<K,V>getMapping(mapIdx);
        if (mapping == null) {
            log.warning("Requested action with unknown mapping", "idx", mapIdx, "key", key);
        } else {
            action.invoke(mapping.get(key));
        }
    }

    // from interface MappingProvider
    public <K,V> void with (ClientObject caller, short mapIdx,
                            RemoteMapping.Action<Mapping<K,V>> action)
    {
        Preconditions.checkState(_isMinister, "Rejecting with() on non-minister [midx=%s]", mapIdx);
        Mapping<K,V> mapping = this.<K,V>getMapping(mapIdx);
        if (mapping == null) {
            log.warning("Requested action with unknown mapping", "idx", mapIdx);
        } else {
            action.invoke(mapping);
        }
    }

    // from interface MappingProvider
    public void sync (ClientObject caller, short mapIdx, Object[] keys, Object[] values)
    {
        LocalMapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to sync unknown mapping [idx=%s]", mapIdx);
        for (int ii = 0; ii < keys.length; ii++) {
            m.sync(keys[ii], values[ii]);
        }
    }

    // from interface Lifecycle.ShutdownComponent
    public void shutdown ()
    {
        _flusher.cancel();  // stop our flushing interval
        broadcastUpdates(); // flush everything one final time
    }

    /**
     * Called by the peer manager during server initialization.
     */
    protected void init (PeerManager peermgr)
    {
        _peermgr = peermgr;
        _peermgr.getNodeObject().setMappingService(
            _invmgr.registerProvider(this, MappingMarshaller.class));
    }

    /**
     * Called by the PeerManager when a new peer connects to the network.
     */
    protected void connectedToPeer (NodeObject nodeobj)
    {
        boolean isMinister = updateMinistry();
        log.info("New peer, updated ministry", "mcount", _ministers.size(),
                 "pcount", _proles.size(), "isMinister", isMinister);

        // when a new peer starts up, it will assume it is a minister until it joins the network;
        // at that point, it must forward any accumulated mappings to a real minister so that they
        // can be properly distributed
        if (!isMinister && _isMinister) {
            // TODO
        }

        _isMinister = isMinister;
    }

    /**
     * Called by the PeerManager when a peer disconnects from the network.
     */
    protected void disconnectedFromPeer (NodeObject nodeobj)
    {
        boolean isMinister = updateMinistry();
        log.info("Lost peer, updated ministry", "ministers", _ministers, "proles", _proles,
                 "isMinister", isMinister);

        // if an existing minister has departed the network, we may become a minister by virtue of
        // being one of the oldest peers in the network; in that case we need to fetch the current
        // value of all non-broadcast mappings from another minister
        if (isMinister && !_isMinister) {
            // TODO
        }

        _isMinister = isMinister;
    }

    /**
     * Updates our {@link #_ministers} and {@link #_proles} lists and returns whether or not we're
     * a minister.
     */
    protected boolean updateMinistry ()
    {
        List<NodeObject> nodes = NODE_ORDER.sortedCopy(_peermgr.getNodeObjects());
        // partition the nodes into ministers and proles; wouldn't it be nice if this was just:
        // (_ministers, _proles) = nodes.splitAt(MINISTER_COUNT)
        _ministers.clear();
        Iterables.addAll(_ministers, Iterables.limit(nodes, MINISTER_COUNT));
        _proles.clear();
        Iterables.addAll(_proles, Iterables.skip(nodes, MINISTER_COUNT));
        return _ministers.contains(_peermgr.getNodeObject());
    }

    protected MappingConfig requireConfig (String id)
    {
        MappingConfig config = _configs.get(id);
        Preconditions.checkArgument(config != null, "Unknown mapping '" + id + "'");
        return config;
    }

    /**
     * Called every {@link #BROADCAST_INTERVAL} milliseconds to determine whether we have mapping
     * updates that need to be broadcast to the proles.
     */
    protected void broadcastUpdates ()
    {
        long now = System.currentTimeMillis();
        for (LocalMapping<?,?> mapping : _mappings.values()) {
            if (!mapping.updates.isEmpty() &&
                mapping.config.repl.shouldBroadcast(now-mapping.lastFlush)) {
                // turn the updates into arrays of keys and values
                Object[] keys = new Object[mapping.updates.size()];
                Object[] values = new Object[keys.length];
                int idx = 0;
                for (Tuple<Object,Object> update : mapping.updates) {
                    keys[idx] = update.left;
                    values[idx] = update.right;
                    idx++;
                }
                // broadcast those updates to all proles
                for (NodeObject prole : _proles) {
                    prole.mappingService.sync(mapping.config.mapIdx, keys, values);
                }
                // note that we've flushed these updates
                mapping.lastFlush = now;
                mapping.updates.clear();
            }
        }
    }

    protected final <K,V> LocalMapping<K,V> getMapping (short mapIdx)
    {
        @SuppressWarnings("unchecked") LocalMapping<K,V> mapping =
            (LocalMapping<K,V>)_mappings.get(mapIdx);
        return mapping;
    }

    protected class LocalMapping<K,V> implements Mapping<K,V>
    {
        public final MappingConfig config;

        /** The underlying data for this map, canonical if we're a master, a cached copy if we're a
         * prole and this is a broadcast map. */
        public final Map<K,V> data = Maps.newHashMap();

        /** A queue of updates waiting to be broadcast for this map. */
        public final List<Tuple<Object,Object>> updates = Lists.newArrayList();

        /** The time at which our updates were last flushed (broadcast). */
        public long lastFlush;

        public LocalMapping (MappingConfig config) {
            this.config = config;
        }

        // from interface Mapping
        public V get (K key) {
            return data.get(key);
        }

        // from interface Mapping
        public V put (K key, V value) {
            V ovalue = data.put(key, value); // update our local mapping

            if (_isMinister) {
                // queue up a sync
                updates.add(Tuple.<Object,Object>newTuple(key, value));
                // notify our listeners directly
                notifyPut(key, value, ovalue);

            } else {
                // forward the update to a minister
                // TODO: pick randomly? cope if we have no ministers?
                _ministers.get(0).mappingService.put(config.mapIdx, key, value);
                // note this put in our local shadow table, we'll flush the shadow table entry (and
                // notify our listeners) when a minister syncs this key back to us
                notePending(key, ovalue);
            }

            return ovalue;
        }

        // from interface Mapping
        public V remove (K key) {
            V ovalue = data.remove(key); // update our local mapping

            if (_isMinister) {
                // queue up a sync
                updates.add(Tuple.<Object,Object>newTuple(key, null));
                // notify our listeners
                notifyRemove(key, ovalue);

            } else {
                // forward the remove to a minister
                _ministers.get(0).mappingService.remove(config.mapIdx, key);
                // note this remove in our local shadow table, we'll flush the shadow table entry
                // (and notify our listeners) when a minister syncs this key back to us
                notePending(key, ovalue);
            }

            return ovalue;
        }

        // from interface Mapping
        public Set<Map.Entry<K,V>> entrySet () {
            return Collections.unmodifiableMap(data).entrySet();
        }

        // from interface Mapping
        public void addListener (Listener<K,V> listener) {
            _listeners.add(listener);
        }

        // from interface XXX
        public void removeListener (Listener<K,V> listener) {
            _listeners.remove(listener);
        }

        /**
         * Applies sync data from a minister to this mapping. We can't just call {@link #put} or
         * {@link #remove} as those would rebroadcast the changed data back to a minister.
         */
        protected void sync (K key, V value) {
            if (value == null) {
                notifyRemove(key, getOldValue(key, data.remove(key)));
            } else {
                notifyPut(key, value, getOldValue(key, data.put(key, value)));
            }
        }

        protected void notifyPut (K key, V value, V ovalue) {
            for (Mapping.Listener<K,V> l : _listeners) {
                try {
                    l.entryPut(key, value, ovalue);
                } catch (Throwable t) {
                    log.warning("Mapping.Listener put failure", "key", key, "lner", l, t);
                }
            }
        }

        protected void notifyRemove (K key, V ovalue) {
            for (Mapping.Listener<K,V> l : _listeners) {
                try {
                    l.entryRemoved(key, ovalue);
                } catch (Throwable t) {
                    log.warning("Mapping.Listener remove failure", "key", key, "lner", l, t);
                }
            }
        }

        // if we did a put/remove locally, we will have cached the old value in our pending table,
        // and we want to use that when the sync finally arrives and the time comes to notify our
        // listeners
        protected V getOldValue (K key, V ovalue) {
            V pvalue = _pending.remove(key);
            return (pvalue == null) ? ovalue : pvalue;
        }

        protected void notePending (K key, V ovalue) {
            V opending = _pending.put(key, ovalue);
            if (opending != null) {
                log.warning("Overwrote pending mapping update, this means someone is put()ing " +
                            "and/or remove()ing too rapidly; expect funny business", "map", config.id,
                            "key", key, "newOldValue", ovalue, "oldOldValue", opending);
            }
        }

        protected final List<Listener<K,V>> _listeners = Lists.newArrayList();

        /** A mapping of puts and removes for which we expect to hear back from a minister. */
        protected Map<K,V> _pending = Maps.newHashMap();
    }

    protected class ProxiedMapping<K,V> implements RemoteMapping<K,V>
    {
        public final MappingConfig config;

        public ProxiedMapping (MappingConfig config) {
            this.config = config;
        }

        // from interface RemoteMapping<K,V>
        public void put (K key, V value) {
        }

        // from interface RemoteMapping<K,V>
        public void get (K key, Callback<V> callback) {
        }

        // from interface RemoteMapping<K,V>
        public void remove (K key) {
        }

        // from interface RemoteMapping<K,V>
        public void withValue (K key, Action<V> action) {
        }

        // from interface RemoteMapping<K,V>
        public void with (K key, Action<Mapping<K,V>> action) {
        }
    }

    protected class MappingConfig {
        /** The string identifier for this mapping. */
        public final String id;

        /** The (assigned) short index of this mapping. */
        public final Short mapIdx;

        /** The replication policy for this mapping. */
        public final ReplicationPolicy repl;

        public MappingConfig (String id, ReplicationPolicy repl) {
            this.id = id;
            this.mapIdx = ++_nextMapIdx;
            this.repl = repl;
        }
    }

    protected static class ImmediateRebroadcast extends ReplicationPolicy {
        @Override public boolean isRemote () {
            return false;
        }
        @Override public boolean shouldBroadcast (long elapsedMillis) {
            return true;
        }
    }

    protected static class BatchedRebroadcast extends ReplicationPolicy {
        public final int batchMillis;
        public BatchedRebroadcast (int batchMillis) {
            this.batchMillis = batchMillis;
        }
        @Override public boolean isRemote () {
            return false;
        }
        @Override public boolean shouldBroadcast (long elapsedMillis) {
            return elapsedMillis > batchMillis;
        }
    }

    protected static class NoRebroadcast extends ReplicationPolicy {
        @Override public boolean isRemote () {
            return true;
        }
        @Override public boolean shouldBroadcast (long elapsedMillis) {
            return false;
        }
    }

    protected Map<String,MappingConfig> _configs = Maps.newHashMap();
    protected Map<Short,LocalMapping<?,?>> _mappings = Maps.newHashMap();

    /** Whether or not we're a minister or a prole. */
    protected boolean _isMinister = true;

    /** A list of all mapping servers (ministers). */
    protected List<NodeObject> _ministers = Lists.newArrayList();

    /** A list of all mapping clients (proles). */
    protected List<NodeObject> _proles = Lists.newArrayList();

    /** Used to assign 16-bit identifiers to each mapping. */
    protected short _nextMapIdx = 0;

    /** The peer manager we work for, can't inject due to circular depends. */
    protected PeerManager _peermgr;

    /** Our flushing interval. */
    protected Interval _flusher;

    // dependencies
    @Inject protected InvocationManager _invmgr;

    /** The number of primary mapping servers maintained in the network.
     * Chiefly a redundancy measure. */
    protected static final int MINISTER_COUNT = 1;

    /** The frequency with which we check whether updates need broadcasting. */
    protected static final long BROADCAST_INTERVAL = 250;

    /** Sorts nodes in order from longest running to most recently started. The (N) longest running
     * nodes are granted minister status. */
    protected static final Ordering<NodeObject> NODE_ORDER = new Ordering<NodeObject>() {
        public int compare (NodeObject n1, NodeObject n2) {
            return Longs.compare(n1.bootStamp, n2.bootStamp);
        }
    }.compound(new Ordering<NodeObject>() {
        public int compare (NodeObject n1, NodeObject n2) {
            return n1.nodeName.compareTo(n2.nodeName);
        }
    });
}
