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

package com.threerings.media.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A mirage implementation that contains no image data. Generally only
 * useful for testing.
 */
public class BlankMirage implements Mirage
{
    public BlankMirage (int width, int height)
    {
        _width = width;
        _height = height;
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, int x, int y)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public int getWidth ()
    {
        return _width;
    }

    // documentation inherited from interface
    public int getHeight ()
    {
        return _height;
    }

    // documentation inherited from interface
    public boolean hitTest (int x, int y)
    {
        return false;
    }

    // documentation inherited from interface
    public long getEstimatedMemoryUsage ()
    {
        return 0;
    }

    // documentation inherited from interface
    public BufferedImage getSnapshot ()
    {
        return null;
    }

    protected int _width;
    protected int _height;
}
