//
// $Id: DirtyItemList.java,v 1.1 2001/10/13 01:08:59 shaper Exp $

package com.threerings.media.sprite;

import java.util.*;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.ObjectTile;

import com.threerings.media.Log;

/**
 * The dirty item list keeps track of dirty sprites and object tiles
 * in a scene.  Since scenes can only ever have one of either item at
 * any given coordinate, dirty items are stored and checked for
 * equality based solely on their coordinate, and each coordinate can
 * exist in the list only once.
 */
public class DirtyItemList extends ArrayList
{
    /**
     * Append the dirty item at the given coordinates to the dirty
     * item list.
     */
    public boolean appendDirtyItem (Object item, int x, int y)
    {
        // only allow appending sprites and object tiles
        if (!(item instanceof Sprite) && !(item instanceof ObjectTile)) {
            return false;
        }

        // only add the item if there are no existing items
        if (!contains(x, y)) {
            add(new DirtyItem(item, x, y));
            return true;
        }

        return false;
    }

    /**
     * Sort the items in the list using the given comparator.
     */
    public void sort (Comparator comp)
    {
        Object[] items = new Object[size()];
        toArray(items);
        Arrays.sort(items, comp);
        clear();
        for (int ii = 0; ii < items.length; ii++) {
            add(items[ii]);
        }
    }

    /**
     * Returns whether the list contains a dirty item at the given
     * coordinates.
     */
    protected boolean contains (int x, int y)
    {
        int size = size();
        for (int ii = 0; ii < size; ii++) {
            Object o = get(ii);
            if (o instanceof DirtyItem) {
                DirtyItem di = (DirtyItem)o;
                return (x == di.x && y == di.y);
            }
        }

        return false;
    }

    /**
     * A wrapper class to hold the items inserted in the dirty list
     * along with their coordinates in the scene.
     */
    public class DirtyItem
    {
        public Object obj;
        public int x, y;

        public DirtyItem (Object obj, int x, int y)
        {
            this.obj = obj;
            this.x = x;
            this.y = y;
        }
    }
}
