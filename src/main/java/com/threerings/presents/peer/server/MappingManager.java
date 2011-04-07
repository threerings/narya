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
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.client.Mapping;
import com.threerings.presents.peer.client.RemoteMapping;
import com.threerings.presents.peer.data.MappingMarshaller;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

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
 * <p>The mapping subsystem selects a subset of the peers in the full network to act as mapping
 * hosts. Sufficiently many peers are selected to allow for fault tolerance in the event of peer
 * failure, but sufficiently few to avoid undue overhead of copying data around the network. The
 * precise subset of peers that are turned into mapping servers is configurable via {@link
 * #TODO}.</p>
 *
 * <p>Mapping configuration must be accomplished during the server initialization period,
 * <em>before</em> the server begins operation. This means that managers which require mappings
 * should inject the {@link MappingManager} into their constructor and {@link #configure} their
 * mappings at construct time.</p>
 */
@Singleton
public class MappingManager
    implements MappingProvider
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
         * to peers every {@code batchSeconds} seconds. This saves the overhead of numerous
         * invidual communications between peers, at the expense that the information available on
         * each peer may be slightly out of date. */
        public static ReplicationPolicy batchedRebroadcast (int batchSeconds) {
            return new BatchedRebroadcast(batchSeconds);
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

        /*package*/ ReplicationPolicy () {} // no extending outside this package
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
     * Adds a listener to the specified mapping.
     */
    public <K,V> void addListener (Mapping.Id<K,V> id, Mapping.Listener<K,V> listener)
    {
        _listeners.put(id.id, listener);
    }

    /**
     * Removes a listener from the specified mapping.
     */
    public <K,V> void removeListener (Mapping.Id<K,V> id, Mapping.Listener<K,V> listener)
    {
        _listeners.remove(id.id, listener);
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
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to get from unknown mapping [idx=%s, key=%s]", mapIdx, key);
        // TODO: check that we're a master?
        rl.requestProcessed(m.get(key));
    }

    // from interface MappingProvider
    public void put (ClientObject caller, short mapIdx, Object key, Object value)
    {
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to put into unknown mapping [idx=%s, key=%s]", mapIdx, key);
        // TODO: check that we're a master?
        m.put(key, value);
    }

    // from interface MappingProvider
    public void remove (ClientObject caller, short mapIdx, Object key)
    {
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to remove from unknown mapping [idx=%s, key=%s]", mapIdx, key);
        // TODO: check that we're a master?
        m.remove(key);
    }

    // from interface MappingProvider
    public <K,V> void withValue (ClientObject caller, short mapIdx, K key,
                                 RemoteMapping.Action<V> action)
    {
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
        Mapping<Object,Object> m = Preconditions.checkNotNull(
            this.<Object,Object>getMapping(mapIdx),
            "Requested to sync unknown mapping [idx=%s]", mapIdx);
        for (int ii = 0; ii < keys.length; ii++) {
            if (values[ii] == null) {
                m.remove(keys[ii]);
            } else {
                m.put(keys[ii], values[ii]);
            }
        }
    }

    /**
     * Called by the peer manager during server initialization.
     */
    protected void init (NodeObject nodeobj)
    {
        nodeobj.setMappingService(_invmgr.registerProvider(this, MappingMarshaller.class));
    }

    protected Iterable<NodeObject> getMinisters ()
    {
        return null; // TODO
    }

    protected Iterable<NodeObject> getProles ()
    {
        return null; // TODO
    }

    protected MappingConfig requireConfig (String id)
    {
        MappingConfig config = _configs.get(id);
        Preconditions.checkArgument(config != null, "Unknown mapping '" + id + "'");
        return config;
    }

    protected final <K,V> Mapping<K,V> getMapping (short mapIdx)
    {
        @SuppressWarnings("unchecked") Mapping<K,V> mapping = (Mapping<K,V>)_mappings.get(mapIdx);
        return mapping;
    }

    protected final <K,V> Collection<Mapping.Listener<K,V>> getListeners (String id)
    {
        // for some reason we can't go straight from Collection<Mapping.Listener<?,?>> to
        // Collection<Mapping.Listener<K,V>> so we have to route through Object first...
        Object list = _listeners.get(id);
        @SuppressWarnings("unchecked") Collection<Mapping.Listener<K,V>> listeners =
            (Collection<Mapping.Listener<K,V>>)list;
        return listeners;
    }

    protected class LocalMapping<K,V> implements Mapping<K,V>
    {
        public final MappingConfig config;
        public final Map<K,V> data = Maps.newHashMap();

        public LocalMapping (MappingConfig config) {
            this.config = config;
        }

        // from interface Mapping
        public V get (K key) {
            return data.get(key);
        }

        // from interface Mapping
        public V put (K key, V value) {
            // update our local mapping
            V ovalue = data.put(key, value);

            // notify our listeners
            for (Mapping.Listener<K,V> l : MappingManager.this.<K,V>getListeners(config.id)) {
                try {
                    l.entryPut(key, value, ovalue);
                } catch (Throwable t) {
                    log.warning("Mapping.Listener put failure", "key", key, "lner", l, t);
                }
            }

            // if we're a minister, queue up a sync
            // if we're a prole, forward the update to a minister
            return ovalue;
        }

        // from interface Mapping
        public V remove (K key) {
            V ovalue = data.remove(key);

            // notify our listeners
            for (Mapping.Listener<K,V> l : MappingManager.this.<K,V>getListeners(config.id)) {
                try {
                    l.entryRemoved(key, ovalue);
                } catch (Throwable t) {
                    log.warning("Mapping.Listener remove failure", "key", key, "lner", l, t);
                }
            }

            // if we're a minister, queue up a sync
            // if we're a prole, forward the update to a minister
            return ovalue;
        }

        // from interface Mapping
        public Set<Map.Entry<K,V>> entrySet () {
            return Collections.unmodifiableMap(data).entrySet();
        }
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
    }

    protected static class BatchedRebroadcast extends ReplicationPolicy {
        public final int batchSeconds;
        public BatchedRebroadcast (int batchSeconds) {
            this.batchSeconds = batchSeconds;
        }
        @Override public boolean isRemote () {
            return false;
        }
    }

    protected static class NoRebroadcast extends ReplicationPolicy {
        @Override public boolean isRemote () {
            return true;
        }
    }

    protected Map<String,MappingConfig> _configs = Maps.newHashMap();
    protected Map<Short,Mapping<?,?>> _mappings = Maps.newHashMap();
    protected Multimap<String,Mapping.Listener<?,?>> _listeners = ArrayListMultimap.create();

    /** Used to assign 16-bit identifiers to each mapping. */
    protected short _nextMapIdx = 0;

    @Inject protected InvocationManager _invmgr;
}
