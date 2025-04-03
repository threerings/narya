//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.io.IOException;

import com.samskivert.util.ArrayIntSet;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link ArrayIntSet} extension that can be streamed.
 */
public class StreamableArrayIntSet extends ArrayIntSet
    implements Streamable
{
    // documentation inherited
    public StreamableArrayIntSet (int[] values)
    {
        super(values);
    }

    // documentation inherited
    public StreamableArrayIntSet (int initialCapacity)
    {
        super(initialCapacity);
    }

    // documentation inherited
    public StreamableArrayIntSet ()
    {
        super();
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_size);
        for (int ii = 0; ii < _size; ii++) {
            out.writeInt(_values[ii]);
        }
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        _size = in.readInt();
        _values = new int[Math.max(_size, DEFAULT_CAPACITY)];
        for (int ii = 0; ii < _size; ii++) {
            _values[ii] = in.readInt();
        }
    }
}
