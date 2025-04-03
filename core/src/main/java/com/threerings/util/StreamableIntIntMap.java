//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.io.IOException;

import com.samskivert.util.IntIntMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link IntIntMap} extension that can be streamed. The keys and
 * values in the map must also be of streamable types.
 *
 * @see Streamable
 */
public class StreamableIntIntMap extends IntIntMap
    implements Streamable
{
    /**
     * Constructs an empty int int map with the specified number of
     * buckets.
     */
    public StreamableIntIntMap (int buckets)
    {
        super(buckets);
    }

    /**
     * Constructs an empty hash int map with the default number of hash
     * buckets.
     */
    public StreamableIntIntMap ()
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
        for (IntIntEntry entry : entrySet()) {
            out.writeInt(entry.getIntKey());
            out.writeInt(entry.getIntValue());
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
            put(key, in.readInt());
        }
    }
}
