//
// $Id: DirtyItemList.java,v 1.2 2001/10/17 22:13:53 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Shape;
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
     * Appends the dirty sprite at the given coordinates to the dirty
     * item list if no item already exists at those coordinates.
     * Returns whether the item was added to the list.
     */
    public boolean appendDirtySprite (Sprite sprite, int x, int y)
    {
        if (!contains(x, y)) {
            add(new DirtyItem(sprite, null, x, y));
            return true;
        }

        return false;
    }

    /**
     * Appends the dirty object tile at the given coordinates to the
     * dirty item list if no item already exists at those coordinates.
     * Returns whether the item was added to the list.
     */
    public boolean appendDirtyObject (
        ObjectTile tile, Shape bounds, int x, int y)
    {
        if (!contains(x, y)) {
            add(new DirtyItem(tile, bounds, x, y));
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
        public Shape bounds;
        public int x, y;

        public DirtyItem (Object obj, Shape bounds, int x, int y)
        {
            this.obj = obj;
            this.bounds = bounds;
            this.x = x;
            this.y = y;
        }

        public void paint (Graphics2D gfx)
        {
            if (obj instanceof Sprite) {
                ((Sprite)obj).paint(gfx);

            } else {
                ((ObjectTile)obj).paint(gfx, bounds);
            }
        }
    }
}
