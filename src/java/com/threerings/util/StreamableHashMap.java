//
// $Id: StreamableHashMap.java,v 1.1 2003/05/27 22:48:31 mdb Exp $

package com.threerings.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link HashMap} extension that can be streamed. The keys and values
 * in the map must also be of streamable types.
 *
 * @see Streamable
 */
public class StreamableHashMap extends HashMap
{
    /**
     * Constructs an empty hash map with the specified number of hash
     * buckets.
     */
    public StreamableHashMap (int buckets, float loadFactor)
    {
        super(buckets, loadFactor);
    }

    /**
     * Constructs an empty hash map with the default number of hash
     * buckets.
     */
    public StreamableHashMap ()
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
        for (Iterator iter = keySet().iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            out.writeObject(key);
            out.writeObject(get(key));
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
            put(in.readObject(), in.readObject());
        }
    }
}
