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

import java.util.Map;
import java.util.Set;

/**
 * An interface to a mapping which is mirrored out to all servers (using a policy specific to each
 * mapping). Users of this mapping can simply look up values as if it were a local map, and updates
 * are automatically delivered to the info servers and propagated out to all other peers. Note that
 * mappings can be configured to immediately reflect updates on the local server, or not, so be
 * aware of the semantics in effect for a given mapping.
 */
public interface Mapping<K,V>
{
    /** Used to identify a mapping and to track its key and value types. One should define a single
     * {@code Id} class as a static final variable somewhere and use that when referencing a
     * particular mapping, to ensure that type safety is observed. */
    public static final class Id<K,V>
    {
        /** The string identifier for this mapping. */
        public final String id;

        public Id (String id) {
            this.id = id;
        }
    }

    /** Used to listen for changes to a mapping. */
    public interface Listener<K,V>
    {
        /** Called when an entry is added to the mapping, or updated. {@code oldValue} will be null
         * for a newly added entry. */
        void entryPut (K key, V value, V oldValue);

        /** Called when an entry is removed from the mapping. */
        void entryRemoved (K key, V oldValue);
    }

    /**
     * Returns the value to which the supplied key is mapped, or null if no mapping exists
     * (mappings are not designed to allow storage of null values).
     */
    V get (K key);

    /**
     * Adds a new mapping if one does not yet exist, or updates any existing mapping.
     * @return the old value mapping to the specified key, or null if no mapping existed.
     */
    V put (K key, V value);

    /**
     * Removes the mapping with the specified key.
     * @return the value mapped to the specified key prior to removal, or null if no mapping
     * existed.
     */
    V remove (K key);

    /**
     * Returns an unmodifiable view of all mappings as a set.
     */
    Set<Map.Entry<K,V>> entrySet ();

    /**
     * Adds a listener to this mapping.
     */
    void addListener (Listener<K,V> listener);

    /**
     * Removes a listener from this mapping.
     */
    void removeListener (Listener<K,V> listener);
}
