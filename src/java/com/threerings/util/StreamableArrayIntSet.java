//
// $Id: StreamableArrayIntSet.java,v 1.2 2004/01/17 01:55:46 ray Exp $

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
        out.writeObject(_values);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        _size = in.readInt();
        _values = (int[]) in.readObject();
    }
}
