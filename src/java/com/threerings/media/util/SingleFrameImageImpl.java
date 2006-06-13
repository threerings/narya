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

package com.threerings.media.util;

import java.awt.Graphics2D;

import com.threerings.media.image.Mirage;

/**
 * The single frame image class is a basic implementation of the {@link
 * MultiFrameImage} interface intended to facilitate the creation of MFIs
 * whose display frames consist of only a single image.
 */
public class SingleFrameImageImpl implements MultiFrameImage
{
    /**
     * Constructs a single frame image object.
     */
    public SingleFrameImageImpl (Mirage mirage)
    {
        _mirage = mirage;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return 1;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _mirage.getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _mirage.getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _mirage.paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _mirage.hitTest(x, y);
    }

    /** The frame image. */
    protected Mirage _mirage;
}
