//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.awt.Point;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A point that can be sent over the network.
 */
public class StreamablePoint extends Point
    implements Streamable
{
    // Some handy constructors

    public StreamablePoint ()
    {
        super();
    }

    public StreamablePoint (int x, int y)
    {
        super(x, y);
    }

    public StreamablePoint (Point p)
    {
        super(p);
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out) throws IOException
    {
        out.writeInt(x);
        out.writeInt(y);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        setLocation(in.readInt(), in.readInt());
    }
}
