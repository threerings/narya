//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util;

import java.lang.reflect.Constructor;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

/**
 * Provides a map implementation that automatically creates, and inserts into the map, a default
 * value for any value retrieved via the {@link #fetch} method which has no entry for that key.
 */
public class DefaultMap<K, V> extends ForwardingMap<K, V>
{
    /** Used to create default values. */
    public interface Creator<K, V>
    {
        /** Creates a new default value for the specified key. */
        V create (K key);
    }

    /**
     * Creates a default map backed by a {@link HashMap} that creates instances of the supplied
     * class (using its no-args constructor) as defaults.
     */
    public static <K, V> DefaultMap<K, V> newInstanceHashMap (Class<V> clazz)
    {
        return newInstanceMap(Maps.<K, V>newHashMap(), clazz);
    }

    /**
     * Creates a default map backed by the supplied map that creates instances of the supplied
     * class (using its no-args constructor) as defaults.
     */
    public static <K, V> DefaultMap<K, V> newInstanceMap (Map<K, V> delegate, Class<V> clazz)
    {
        Creator<K, V> creator = newInstanceCreator(clazz);
        return newMap(delegate, creator);
    }


    /**
     * Returns a Creator that makes instances of the supplied class (using its no-args
     * constructor) as default values.
     */
    public static <K, V> Creator<K, V> newInstanceCreator (Class<V> clazz) {

        final Constructor<V> ctor;
        try {
            ctor = clazz.getConstructor();
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException(clazz + " must have a no-args constructor.");
        }
        return new Creator<K, V>() {
            public V create (K key) {
                try {
                    return ctor.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Creates a default map backed by a {@link HashMap} using the supplied default creator.
     */
    public static <K, V> DefaultMap<K, V> newHashMap (Creator<K, V> creator)
    {
        return newMap(Maps.<K, V>newHashMap(), creator);
    }

    /**
     * Creates a default map backed by the supplied map using the supplied default creator.
     */
    public static <K, V> DefaultMap<K, V> newMap (Map<K, V> delegate, Creator<K, V> creator)
    {
        return new DefaultMap<K, V>(delegate, creator);
    }

    /**
     * Creates a default map backed by the supplied map using the supplied default creator.
     */
    public DefaultMap (Map<K, V> delegate, Creator<K, V> creator)
    {
        _delegate = delegate;
        _creator = creator;
    }

    /**
     * Looks up the supplied key in the map, returning the value to which it is mapped. If the key
     * has no mapping, a default value will be obtained for the requested key and placed into the
     * map. The current and subsequent lookups will return that value.
     */
    public V fetch (K key)
    {
        V value = get(key);
        // null is a valid value, so we check containsKey() before creating a default
        if (value == null && !containsKey(key)) {
            put(key, value = _creator.create(key));
        }
        return value;
    }

    @Override // from ForwardingMap
    protected Map<K, V> delegate()
    {
        return _delegate;
    }

    protected final Map<K, V> _delegate;
    protected final Creator<K, V> _creator;
}
