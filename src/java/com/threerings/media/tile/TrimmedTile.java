//
// $Id: TrimmedTile.java,v 1.7 2004/08/27 02:12:41 mdb Exp $
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
import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

/**
 * Behaves just like a regular tile, but contains a "trimmed" image which
 * is one where the source image has been trimmed to the smallest
 * rectangle that contains all the non-transparent pixels of the original
 * image.
 */
public class TrimmedTile extends Tile
{
    /**
     * Sets the trimmed bounds of this tile.
     *
     * @param tbounds contains the width and height of the
     * <em>untrimmed</em> tile, but the x and y offset of the
     * <em>trimmed</em> tile image in the original untrimmed tile image.
     */
    public void setTrimmedBounds (Rectangle tbounds)
    {
        _tbounds = tbounds;
    }

    // documentation inherited
    public int getWidth ()
    {
        return _tbounds.width;
    }

    // documentation inherited
    public int getHeight ()
    {
        return _tbounds.height;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, int x, int y)
    {
        _mirage.paint(gfx, x + _tbounds.x, y + _tbounds.y);
    }

    /**
     * Fills in the bounds of the trimmed image within the coordinate
     * system defined by the complete virtual tile.
     */
    public void getTrimmedBounds (Rectangle tbounds)
    {
        tbounds.setBounds(_tbounds.x, _tbounds.y,
                          _mirage.getWidth(), _mirage.getHeight());
    }

    // documentation inherited
    public boolean hitTest (int x, int y)
    {
        return super.hitTest(x - _tbounds.x, y - _tbounds.y);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
	buf.append(", tbounds=").append(StringUtil.toString(_tbounds));
    }

    /** Our extra trimmed image dimension information. */
    protected Rectangle _tbounds;
}
