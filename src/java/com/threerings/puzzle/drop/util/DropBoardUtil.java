//
// $Id: DropBoardUtil.java,v 1.2 2004/02/25 14:48:44 mdb Exp $

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
