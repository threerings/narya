//
// $Id: StreamableIntIntMap.java,v 1.1 2003/04/22 23:36:56 eric Exp $

package com.threerings.util;

import java.io.IOException;
import java.util.Iterator;

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
        Iterator iter = keys();
        while (iter.hasNext()) {
            int key = ((Integer)iter.next()).intValue();
            out.writeInt(key);
            out.writeInt(get(key));
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
            put(key, in.readInt());
        }
    }
}
