//
// $Id: StreamableHashIntMap.java,v 1.1 2003/02/18 03:01:16 mdb Exp $

package com.threerings.util;

import java.io.IOException;
import java.util.Iterator;

import com.samskivert.util.HashIntMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link HashIntMap} extension that can be streamed. The keys and
 * values in the map must also be of streamable types.
 *
 * @see Streamable
 */
public class StreamableHashIntMap extends HashIntMap
    implements Streamable
{
    /**
     * Constructs an empty hash int map with the specified number of hash
     * buckets.
     */
    public StreamableHashIntMap (int buckets, float loadFactor)
    {
        super(buckets, loadFactor);
    }

    /**
     * Constructs an empty hash int map with the default number of hash
     * buckets.
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
        Iterator iter = keySet().iterator();
        while (iter.hasNext()) {
            int key = ((Integer)iter.next()).intValue();
            out.writeInt(key);
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
            int key = in.readInt();
            put(key, in.readObject());
        }
    }
}
