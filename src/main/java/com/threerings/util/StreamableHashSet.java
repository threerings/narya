//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
