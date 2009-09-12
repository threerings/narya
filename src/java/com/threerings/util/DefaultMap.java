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
 * value for keys with no value when a value is fetched via the {@link #get} method. Note that this
 * map must assume that all keys supplied to {@link #get} are valid instances of the type specified
 * by K as those keys will be used in a subsequent call to {@link #put}. Thus you must not call
 * {@link #get} with a key of invalid type or you will cause your map to become unsound.
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
        final Constructor<V> ctor;
        try {
            ctor = clazz.getConstructor();
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException(clazz + " must have a no-args constructor.");
        }
        return newMap(delegate, new Creator<K, V>() {
            public V create (K key) {
                try {
                    return ctor.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
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

    @Override // from Map
    public V get (Object key)
    {
        @SuppressWarnings("unchecked") K tkey = (K)key;
        V value = super.get(key);
        // null is a valid value, so we check containsKey() before creating a default
        if (value == null && !containsKey(key)) {
            put(tkey, value = _creator.create(tkey));
        }
        return value;
    }

    protected DefaultMap (Map<K, V> delegate, Creator<K, V> creator)
    {
        _delegate = delegate;
        _creator = creator;
    }

    @Override // from ForwardingMap
    protected Map<K, V> delegate()
    {
        return _delegate;
    }

    protected final Map<K, V> _delegate;
    protected final Creator<K, V> _creator;
}
