//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
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
public class StreamableHashMap<K,V> extends HashMap<K,V>
    implements Streamable
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
        for (Iterator<Map.Entry<K,V>> iter = entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<K,V> entry = (Map.Entry<K,V>) iter.next();
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
