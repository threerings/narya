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

package com.threerings.presents.peer.client;

import com.threerings.io.Streamable;

/**
 * An interface to a mapping which is stored only on the info servers. A user of this mapping must
 * either issue a request for the mapping (via {@link #get}) or package up an action to be
 * performed with the mapping data and deliver it to the mapping server (via {@link #with} and
 * {@link #withValue}).
 */
public interface RemoteMapping<K,V>
{
    /** Used to identify a mapping and to track its key and value types. One should define a single
     * {@code Id} class as a static final variable somewhere and use that when referencing a
     * particular mapping, to ensure that type safety is observed. */
    public static final class Id<K,V> {
        /** The string identifier for this mapping. */
        public final String id;

        public Id (String id) {
            this.id = id;
        }
    }

    /** Used to report collisions and current values. See {@link #get}, etc. */
    public interface Callback<V> {
        void invoke (V value);
    }

    /** Used by {@link #with} and {@link #withValue} for sending code between servers. */
    public interface Action<V> extends Streamable.Closure {
        void invoke (V value);
    }

    /**
     * Adds a new mapping if one does not yet exist, or updates any existing mapping.
     */
    void put (K key, V value);

    /**
     * Fetches the value to which the supplied key is mapped and passes it to the supplied callable
     * (on the local server, but at some point in the future). If no mapping exists for the key,
     * null will be supplied to the callback (mappings are not designed to allow storage of null
     * values).
     */
    void get (K key, Callback<V> callback);

    /**
     * Removes the current value to which {@code key} is mapped.
     */
    void remove (K key);

    /**
     * Sends the supplied action to an info server which hosts this remote mapping, and executes it
     * with the current value associated with the specified key. IF no mapping exists for the key,
     * null will be supplied to the action (mappings are not designed to allow storage of null
     * values).
     */
    void withValue (K key, Action<V> action);

    /**
     * Sends the supplied action to an info server which hosts this remote mapping, and executes it
     * with this entire mapping represented as a local mapping.
     */
    void with (K key, Action<Mapping<K,V>> action);
}
