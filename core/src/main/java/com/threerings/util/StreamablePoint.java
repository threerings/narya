//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.awt.Point;

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
}
