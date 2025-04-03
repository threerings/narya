//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.util.ArrayList;

import java.io.IOException;

import com.samskivert.annotation.ReplacedBy;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * An {@link ArrayList} extension that can be streamed. The contents of the list must also be of
 * streamable types.
 *
 * @see Streamable
 * @param <E> the type of elements stored in this list.
 */
@ReplacedBy("java.util.List")
public class StreamableArrayList<E> extends ArrayList<E>
    implements Streamable
{
    /**
     * Creates an empty StreamableArrayList.
     */
    public static <E> StreamableArrayList<E> newList ()
    {
        return new StreamableArrayList<E>();
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        int ecount = size();
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeObject(get(ii));
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
            @SuppressWarnings("unchecked") E elem = (E)in.readObject();
            add(elem);
        }
    }
}
