//
// $Id: DirtyRectList.java,v 1.1 2001/08/22 02:14:57 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * The dirty rect list is used to maintain a list of dirty rectangles. It
 * differs from a plain old list only in that it provides a convenient
 * mechanism for appending a dirty rectangle to the list if and only if
 * that rectangle is not already on the list.
 */
public class DirtyRectList extends ArrayList
{
    /**
     * Appends the specified dirty rectangle to the list only if a
     * rectangle of the same size is not already in the list.
     *
     * @return true if the rectangle were appended, false if she weren't.
     * Har!
     */
    public boolean appendDirtyRect (Rectangle rect)
    {
        if (contains(rect)) {
            return false;
        } else {
            add(rect);
            return true;
        }
    }
}
