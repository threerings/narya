//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.io.IOException;

import com.samskivert.util.HashIntMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link HashIntMap} extension that can be streamed. The keys and values in the map must also
 * be of streamable types.
 *
 * @see Streamable
 * @param <V> the type of value stored in this map.
 */
public class StreamableHashIntMap<V> extends HashIntMap<V>
    implements Streamable
{
    /**
     * Constructs an empty hash int map with the specified number of hash buckets.
     */
    public StreamableHashIntMap (int buckets, float loadFactor)
    {
        super(buckets, loadFactor);
    }

    /**
     * Constructs an empty hash int map with the default number of hash buckets.
     */
    public StreamableHashIntMap ()
    {
        super();
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        int ecount = size();
        out.writeInt(ecount);
        for (IntEntry<V> entry : intEntrySet()) {
            out.writeInt(entry.getIntKey());
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
        ensureCapacity(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            int key = in.readInt();
            @SuppressWarnings("unchecked") V value = (V)in.readObject();
            put(key, value);
        }
    }
}
