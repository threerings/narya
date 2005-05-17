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

package com.threerings.media.tile;

import java.awt.Graphics2D;

import com.threerings.media.image.Colorization;
import com.threerings.media.util.MultiFrameImage;

/**
 * A {@link MultiFrameImage} implementation that obtains its image frames
 * from a tileset.
 */
public class TileMultiFrameImage implements MultiFrameImage
{
    /**
     * Creates a tile MFI which will obtain its image frames from the
     * specified source tileset.
     */
    public TileMultiFrameImage (TileSet source)
    {
        _source = source;
    }

    /**
     * Creates a recoolored tile MFI which will obtain its image frames
     * from the specified source tileset.
     */
    public TileMultiFrameImage (TileSet source, Colorization[] zations)
    {
        this(source.clone(zations));
    }

    // documentation inherited from interface
    public int getFrameCount ()
    {
        return _source.getTileCount();
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _source.getTile(index).getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _source.getTile(index).getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _source.getTile(index).paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _source.getTile(index).hitTest(x, y);
    }

    protected TileSet _source;
}
