//
// $Id: DirtyItemList.java,v 1.1 2001/10/24 00:55:08 shaper Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.util.*;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.ObjectTile;

import com.threerings.media.Log;

/**
 * The dirty item list keeps track of dirty sprites and object tiles
 * in a scene.
 */
public class DirtyItemList extends ArrayList
{
    /** The dirty item comparator used to sort dirty items back to front. */
    public static final Comparator DIRTY_COMP = new DirtyItemComparator();

    /**
     * Appends the dirty sprite at the given coordinates to the dirty
     * item list.
     */
    public void appendDirtySprite (
        Sprite sprite, int x, int y, Rectangle dirtyRect)
    {
        add(new DirtyItem(sprite, null, x, y, dirtyRect));
    }

    /**
     * Appends the dirty object tile at the given coordinates to the
     * dirty item list.
     */
    public void appendDirtyObject (
        ObjectTile tile, Shape bounds, int x, int y, Rectangle dirtyRect)
    {
        add(new DirtyItem(tile, bounds, x, y, dirtyRect));
    }

    /**
     * Returns an array of the {@link DirtyItem} objects in the list
     * sorted with the {@link DirtyItemComparator}.
     */
    public DirtyItem[] sort ()
    {
        DirtyItem items[] = new DirtyItem[size()];
        toArray(items);
        Arrays.sort(items, DirtyItemList.DIRTY_COMP);
        return items;
    }

    /**
     * A class to hold the items inserted in the dirty list along with
     * all of the information necessary to render their dirty regions
     * to the target graphics context when the time comes to do so.
     */
    public class DirtyItem
    {
        public Object obj;
        public Shape bounds;
        public int x, y;
        public Rectangle dirtyRect;

        /**
         * Constructs a dirty item.
         */
        public DirtyItem (
            Object obj, Shape bounds, int x, int y, Rectangle dirtyRect)
        {
            this.obj = obj;
            this.bounds = bounds;
            this.x = x;
            this.y = y;
            this.dirtyRect = dirtyRect;
        }

        /**
         * Paints the dirty item to the given graphics context.  Only
         * the portion of the item that falls within the given dirty
         * rectangle is actually drawn.
         */
        public void paint (Graphics2D gfx, Rectangle clip)
        {
            Shape oclip = gfx.getClip();

            // clip the draw region to the dirty portion of the item
            gfx.clip(clip);

            // paint the item
            if (obj instanceof Sprite) {
                ((Sprite)obj).paint(gfx);
            } else {
                ((ObjectTile)obj).paint(gfx, bounds);
            }

            // restore original clipping region
            gfx.setClip(oclip);
        }

        public boolean equals (Object other)
        {
            // we're never equal to something that's not our kind
            if (!(other instanceof DirtyItem)) {
                return false;
            }

            // sprites are equivalent if they're the same sprite
            DirtyItem b = (DirtyItem)other;
            if ((obj instanceof Sprite) && (b.obj instanceof Sprite)) {
                return (obj == b.obj);
            }

            // object-to-object or object-to-sprite are distinguished
            // simply by tile coordinate since they can never occupy
            // the same tile
            return (x == b.x && y == b.y);
        }

        public String toString ()
        {
            return "[obj=" + obj + ", x=" + x + ", y=" + y +
                ", drect=" + dirtyRect + "]";
        }
    }

    /**
     * A comparator class for use in sorting the dirty sprites and
     * objects in a scene in ascending x- and y-coordinate order
     * suitable for rendering in the isometric view with proper visual
     * results.
     */
    public static class DirtyItemComparator implements Comparator
    {
        public int compare (Object a, Object b)
        {
            DirtyItem da = (DirtyItem)a;
            DirtyItem db = (DirtyItem)b;

            if (da.x == db.x &&
                da.y == db.y &&
                da.obj != db.obj) {
                // we're comparing two sprites co-existing on the same
                // tile, so study their fine coordinates to determine
                // rendering order
                AmbulatorySprite as = (AmbulatorySprite)da.obj;
                AmbulatorySprite bs = (AmbulatorySprite)db.obj;

                int aloc = as.getFineX() + as.getFineY();
                int bloc = bs.getFineX() + bs.getFineY();

                if (aloc < bloc) {
                    return -1;
                } else if (aloc > bloc) {
                    return 1;
                } else {
                    // if they're at the same vertical row of
                    // intra-tile tiles, just use something consistent
                    return as.hashCode() - bs.hashCode();
                }
            }

            // check whether right edge of a overlaps with left edge of b
            int comp = getRightOverlap(da, db);
            if (comp != 0) {
                return comp;
            }

            // check whether right edge of b overlaps with left edge of a
            comp = getRightOverlap(db, da);
            if (comp != 0) {
                // reverse ordering per reversed overlap check
                return (comp == -1) ? 1 : -1;
            }

            // determine ordering based purely on coordinates
            if (da.x <= db.x && da.y <= db.y) {
                return -1;
            }

            return 1;
        }

        public boolean equals (Object obj)
        {
	    return (obj == this);
        }

        /**
         * Checks the right edge of <code>da</code> to see whether it
         * overlaps with the left edge of <code>db</code>.
         *
         * @return -1 if <code>da</code> should be rendered behind
         * <code>db</code>, 0 if the right edge of <code>da</code>
         * does not overlap with <code>db</code>, and 1 if
         * <code>da</code> should be rendered in front of
         * <code>db</code>.
         */
        protected int getRightOverlap (DirtyItem da, DirtyItem db)
        {
            int ax = da.x, bx = db.x;
            int ay = da.y, by = db.y;

            // get da's rightmost corner coordinate
            if (da.obj instanceof ObjectTile) {
                ay -= (((ObjectTile)da.obj).baseHeight - 1);
            }

            // get db's leftmost corner coordinate
            if (db.obj instanceof ObjectTile) {
                bx -= (((ObjectTile)db.obj).baseWidth - 1);
            }

            if (ax < bx && ay > by) {
                // we most certainly don't overlap
                return 0;
            }

            // calculate inequality constant for db's leftmost corner
            int k = (bx + by);

            // we need to determine whether to render da in front of
            // db, so we check whether da's rightmost corner is above
            // or below db's leftmost corner.
            if (ay <= k - ax) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
