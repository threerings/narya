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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threerings.geom.GeomUtil;

/**
 * A uniform tileset is one that is composed of tiles that are all the
 * same width and height and are arranged into rows, with each row having
 * the same number of tiles except possibly the final row which can
 * contain the same as or less than the number of tiles contained by the
 * previous rows.
 */
public class UniformTileSet extends TileSet
{
    // documentation inherited
    public int getTileCount ()
    {
        BufferedImage tsimg = getRawTileSetImage();
        int perRow = tsimg.getWidth() / _width;
        int perCol = tsimg.getHeight() / _height;
        return perRow * perCol;
    }

    /**
     * Specifies the width of the tiles in this tileset.
     */
    public void setWidth (int width)
    {
        _width = width;
    }

    /**
     * Returns the width of the tiles in this tileset.
     */
    public int getWidth ()
    {
        return _width;
    }

    /**
     * Specifies the height of the tiles in this tileset.
     */
    public void setHeight (int height)
    {
        _height = height;
    }

    /**
     * Returns the height of the tiles in this tileset.
     */
    public int getHeight ()
    {
        return _height;
    }

    // documentation inherited
    protected Rectangle computeTileBounds (int tileIndex)
    {
        BufferedImage tsimg = getRawTileSetImage();
        return GeomUtil.getTile(tsimg.getWidth(), tsimg.getHeight(),
                                _width, _height, tileIndex);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", width=").append(_width);
	buf.append(", height=").append(_height);
    }

    /** The width (in pixels) of the tiles in this tileset. */
    protected int _width;

    /** The height (in pixels) of the tiles in this tileset. */
    protected int _height;

    /** Our historic serialization version id. */
    private static final long serialVersionUID = 3536616655149232917L;
}
