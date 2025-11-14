//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.awt.Rectangle;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link Rectangle} extension that can be streamed.
 */
public class StreamableRectangle extends Rectangle
    implements Streamable
{
    /**
     * Creates a rectangle with the specified coordinates.
     */
    public StreamableRectangle (int x, int y, int width, int height)
    {
        super(x, y, width, height);
    }

    /**
     * Copy constructor.
     */
    public StreamableRectangle (Rectangle rect)
    {
        super(rect);
    }

    /**
     * No-arg constructor for deserialization.
     */
    public StreamableRectangle ()
    {
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out) throws IOException
    {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(width);
        out.writeInt(height);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        setBounds(in.readInt(), in.readInt(), in.readInt(), in.readInt());
    }
}
