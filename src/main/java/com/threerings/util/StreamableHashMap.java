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

package com.threerings.util;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import com.samskivert.annotation.ReplacedBy;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link HashMap} extension that can be streamed. The keys and values in the map must also be
 * of streamable types.
 *
 * @see Streamable
 * @param <K> the type of key stored in this map.
 * @param <V> the type of value stored in this map.
 */
@ReplacedBy("java.util.Map")
public class StreamableHashMap<K, V> extends HashMap<K, V>
    implements Streamable
{
    /**
     * Creates an empty StreamableHashMap.
     */
    public static <K, V> StreamableHashMap<K, V> newMap ()
    {
        return new StreamableHashMap<K, V>();
    }

    /**
     * Creates StreamableHashMap populated with the same values as the provided Map.
     */
    public static <K, V> StreamableHashMap<K, V> newMap (Map<? extends K, ? extends V> map)
    {
        return new StreamableHashMap<K, V>(map);
    }

    /**
     * Constructs an empty hash map with the specified number of hash buckets.
     */
    public StreamableHashMap (int buckets, float loadFactor)
    {
        super(buckets, loadFactor);
    }

    /**
     * Constructs an empty hash map with the default number of hash buckets.
     */
    public StreamableHashMap ()
    {
        super();
    }

    /**
     * Constructs a hash map with the default number of hash buckets, populated with the same
     * values as the provided Map.
     */
    public StreamableHashMap (Map<? extends K, ? extends V> map)
    {
        super(map);
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        int ecount = size();
        out.writeInt(ecount);
        for (Map.Entry<K, V> entry : entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        int ecount = in.readInt();
        for (int ii = 0; ii < ecount; ii++) {
            @SuppressWarnings("unchecked") K key = (K)in.readObject();
            @SuppressWarnings("unchecked") V value = (V)in.readObject();
            put(key, value);
        }
    }
}
