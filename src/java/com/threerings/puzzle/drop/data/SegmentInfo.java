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

package com.threerings.puzzle.drop.data;

import com.samskivert.util.StringUtil;
import com.threerings.util.DirectionCodes;

/**
 * Describes a segment of pieces in a {@link DropBoard}.
 */
public class SegmentInfo
{
    /** The segment's direction; one of {@link DirectionCodes#HORIZONTAL}
     * or {@link DirectionCodes#VERTICAL}. */
    public int dir;

    /** The segment's lower-left board coordinates. */
    public int x, y;

    /** The segment's length in pieces. */
    public int len;

    /**
     * Constructs a segment info object.
     */
    public SegmentInfo (int dir, int x, int y, int len)
    {
        this.dir = dir;
        this.x = x;
        this.y = y;
        this.len = len;
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
