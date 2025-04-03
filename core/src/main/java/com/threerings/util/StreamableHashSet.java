//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.util.HashSet;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link HashSet} extension that can be streamed. The values in the set must also be of
 * streamable types.
 *
 * @see Streamable
 * @param <E> the type of element stored in this set.
 */
public class StreamableHashSet<E> extends HashSet<E>
    implements Streamable
{
    /**
     * Creates an empty StreamableHashSet with the default number of hash buckets.
     */
    public static <E> StreamableHashSet<E> newSet ()
    {
        return new StreamableHashSet<E>();
    }

    /**
     * Constructs an empty hash set with the specified number of hash buckets.
     */
    public StreamableHashSet (int buckets, float loadFactor)
    {
        super(buckets, loadFactor);
    }

    /**
     * Constructs an empty hash set with the default number of hash buckets.
     */
    public StreamableHashSet ()
    {
        super();
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(size());
        for (E value : this) {
            out.writeObject(value);
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
            @SuppressWarnings("unchecked") E value = (E)in.readObject();
            add(value);
        }
    }
}
