//
// $Id: StreamableArrayIntSet.java,v 1.1 2004/01/17 01:14:12 eric Exp $

package com.threerings.util;

import java.io.IOException;
import java.util.Iterator;

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
        int size = size();
        out.writeInt(size);
        Iterator itr = iterator();
        while (itr.hasNext()) {
            int value = ((Integer)itr.next()).intValue();
            out.writeInt(value);
        }
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        int size = in.readInt();
        for (int ii = 0; ii < size; ii++) {
            add(in.readInt());
        }
    }
}
