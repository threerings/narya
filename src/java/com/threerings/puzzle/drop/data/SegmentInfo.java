//
// $Id: SegmentInfo.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

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
