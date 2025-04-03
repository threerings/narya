//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.awt.Rectangle;

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
}
