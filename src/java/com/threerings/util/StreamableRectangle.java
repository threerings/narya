//
// $Id: StreamableTuple.java 4736 2007-06-14 18:19:38Z dhoover $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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
