//
// $Id: DropBoardUtil.java,v 1.3 2004/08/27 02:20:31 mdb Exp $
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

package com.threerings.puzzle.drop.util;

import com.threerings.util.DirectionCodes;

public class DropBoardUtil
    implements DirectionCodes
{
    /**
     * Returns the orientation resulting from rotating the block in the
     * given direction the specified number of times.
     *
     * @param orient the current orientation.
     * @param dir the direction to rotate in; one of <code>CW</code> or
     * <code>CCW</code>.
     * @param count the number of rotations to perform.
     *
     * @return the rotated orientation.
     */
    public static int getRotatedOrientation (int orient, int dir, int count)
    {
        for (int ii = 0; ii < (count % 4); ii++) {
            orient = getRotatedOrientation(orient, dir);
        }
        return orient;
    }

    /**
     * Returns the orientation resulting from rotating the block in
     * the given direction.
     *
     * @param orient the current orientation.
     * @param dir the direction to rotate in; one of <code>CW</code> or
     * <code>CCW</code>.
     *
     * @return the rotated orientation.
     */
    public static int getRotatedOrientation (int orient, int dir)
    {
        return (orient + ((dir == CW) ? 2 : 6)) % DIRECTION_COUNT;
    }
}
