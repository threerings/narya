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

import com.threerings.presents.client.InvocationService;

/**
 * Defines the interactions between mapping managers on different peers.
 */
public interface MappingService extends InvocationService
{
    /**
     * Updates the specified key/value pair in the specified remove mapping.
     */
    void put (short mapIdx, Object key, Object value);

    /**
     * Fetches the mapping for the specified key.
     * @param listener will be notified with the current value of the mapping, or null if no
     * mapping exists.
     */
    void get (short mapIdx, Object key, ResultListener listener);

    /**
     * Removes the mapping with the specified key.
     */
    void remove (short mapIdx, Object key);

    /**
     * Executes the supplied action on the value mapped to the specified key.
     */
    <K,V> void withValue (short mapIdx, K key, RemoteMapping.Action<V> action);

    /**
     * Executes the supplied action with the specified mapping.
     */
    <K,V> void with (short mapIdx, RemoteMapping.Action<Mapping<K,V>> action);

    /**
     * Syncs the supplied key/value pairs to this peer.
     */
    void sync (short mapIdx, Object[] keys, Object[] values);
}
