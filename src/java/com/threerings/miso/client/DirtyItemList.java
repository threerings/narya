//
// $Id: DirtyItemList.java,v 1.29 2004/02/25 14:43:57 mdb Exp $

package com.threerings.miso.client;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.SortableArrayList;

import com.threerings.media.Log;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.ObjectTile;

/**
 * The dirty item list keeps track of dirty sprites and object tiles
 * in a scene.
 */
public class DirtyItemList
{
    /**
     * Creates a dirt item list that will handle dirty items for the
     * specified view.
     */
    public DirtyItemList ()
    {
    }

    /**
     * Appends the dirty sprite at the given coordinates to the dirty item
     * list.
     *
     * @param sprite the dirty sprite itself.
     * @param tx the sprite's x tile position.
     * @param ty the sprite's y tile position.
     */
    public void appendDirtySprite (Sprite sprite, int tx, int ty)
    {
        DirtyItem item = getDirtyItem();
        item.init(sprite, tx, ty);
        _items.add(item);
    }

    /**
     * Appends the dirty object tile at the given coordinates to the dirty
     * item list.
     *
     * @param scene the scene object that is dirty.
     */
    public void appendDirtyObject (SceneObject scobj)
    {
        DirtyItem item = getDirtyItem();
        item.init(scobj, scobj.info.x, scobj.info.y);
        _items.add(item);
    }

    /**
     * Returns the dirty item at the given index in the list.
     */
    public DirtyItem get (int idx)
    {
        return (DirtyItem)_items.get(idx);
    }

    /**
     * Returns an array of the {@link DirtyItem} objects in the list
     * sorted in proper rendering order.
     */
    public void sort ()
    {
        int size = size();

        if (DEBUG_SORT) {
            Log.info("Sorting dirty item list [size=" + size + "].");
        }

        // if we've only got one item, we need to do no sorting
        if (size > 1) {
            // get items sorted by increasing origin x-coordinate
            _xitems.addAll(_items);
            _xitems.sort(ORIGIN_X_COMP);
            if (DEBUG_SORT) {
                Log.info("Sorted by x-origin " +
                         "[items=" + toString(_xitems) + "].");
            }

            // get items sorted by increasing origin y-coordinate
            _yitems.addAll(_items);
            _yitems.sort(ORIGIN_Y_COMP);
            if (DEBUG_SORT) {
                Log.info("Sorted by y-origin " +
                         "[items=" + toString(_yitems) + "].");
            }

            // sort the items according to the depth of the rear-most tile
            _ditems.addAll(_items);
            _ditems.sort(REAR_DEPTH_COMP);

            // now insertion sort the items from back to front into the
            // render-sorted array
            _items.clear();
          POS_LOOP:
            for (int ii = 0; ii < size; ii++) {
                DirtyItem item = (DirtyItem)_ditems.get(ii);
                for (int rr = _items.size()-1; rr >= 0; rr--) {
                    DirtyItem pitem = (DirtyItem)_items.get(rr);
                    // if we render in front of this item, insert
                    // ourselves immediately following it
                    if (_rcomp.compare(item, pitem) > 0) {
                        _items.add(rr+1, item);
                        continue POS_LOOP;
                    }
                }
                // we don't render in front of anyone, so we go at the
                // front of the list
                _items.add(0, item);
            }

            // clear out our temporary arrays
            _xitems.clear();
            _yitems.clear();
            _ditems.clear();
        }

        if (DEBUG_SORT) {
            Log.info("Sorted for render [items=" + toString(_items) + "].");
            for (int ii = 0, ll = _items.size()-1; ii < ll; ii++) {
                DirtyItem a = (DirtyItem)_items.get(ii);
                DirtyItem b = (DirtyItem)_items.get(ii+1);
                if (_rcomp.compare(a, b) > 0) {
                    Log.warning("Invalid ordering [a=" + a + ", b=" + b + "].");
                }
            }
        }
    }

    /**
     * Paints all the dirty items in this list using the supplied graphics
     * context. The items are removed from the dirty list after being
     * painted and the dirty list ends up empty.
     */
    public void paintAndClear (Graphics2D gfx)
    {
        int icount = _items.size();
        for (int ii = 0; ii < icount; ii++) {
            DirtyItem item = (DirtyItem)_items.get(ii);
            item.paint(gfx);
            item.clear();
            _freelist.add(item);
        }
        _items.clear();
    }

    /**
     * Clears out any items that were in this list.
     */
    public void clear ()
    {
        for (int icount = _items.size(); icount > 0; icount--) {
            DirtyItem item = (DirtyItem)_items.remove(0);
            item.clear();
            _freelist.add(item);
        }
    }

    /**
     * Returns the number of items in the dirty item list.
     */
    public int size ()
    {
        return _items.size();
    }

    /**
     * Obtains a new dirty item instance, reusing an old one if possible
     * or creating a new one otherwise.
     */
    protected DirtyItem getDirtyItem ()
    {
        if (_freelist.size() > 0) {
            return (DirtyItem)_freelist.remove(0);
        } else {
            return new DirtyItem();
        }
    }

    /**
     * Returns an abbreviated string representation of the given dirty
     * item describing only its origin coordinates and render priority.
     * Intended for debugging purposes.
     */
    protected static String toString (DirtyItem a)
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf, a);
        return buf.append("]").toString();
    }

    /**
     * Returns an abbreviated string representation of the two given dirty
     * items. See {@link #toString(DirtyItem}.
     */
    protected static String toString (DirtyItem a, DirtyItem b)
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf, a);
        toString(buf, b);
        return buf.append("]").toString();
    }

    /**
     * Returns an abbreviated string representation of the given dirty
     * items. See {@link #toString(DirtyItem}.
     */
    protected static String toString (SortableArrayList items)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int ii = 0; ii < items.size(); ii++) {
            DirtyItem item = (DirtyItem)items.get(ii);
            toString(buf, item);
            if (ii < (items.size() - 1)) {
                buf.append(", ");
            }
        }
        return buf.append("]").toString();
    }

    /** Helper function for {@link #toString(DirtyItem)}. */
    protected static void toString (StringBuffer buf, DirtyItem item)
    {
        buf.append("(o:+").append(item.ox).append("+").append(item.oy);
        buf.append(" p:").append(item.getRenderPriority()).append(")");
    }

    /**
     * A class to hold the items inserted in the dirty list along with
     * all of the information necessary to render their dirty regions
     * to the target graphics context when the time comes to do so.
     */
    public class DirtyItem
    {
        /** The dirtied object; one of either a sprite or an object tile. */
        public Object obj;

        /** The origin tile coordinates. */
        public int ox, oy;

        /** The leftmost tile coordinates. */
        public int lx, ly;

        /** The rightmost tile coordinates. */
        public int rx, ry;

        /**
         * Initializes a dirty item.
         */
        public void init (Object obj, int x, int y)
        {
            this.obj = obj;
            this.ox = x;
            this.oy = y;

            // calculate the item's leftmost and rightmost tiles; note
            // that sprites occupy only a single tile, so leftmost and
            // rightmost tiles are equivalent
            lx = rx = ox;
            ly = ry = oy;
            if (obj instanceof SceneObject) {
                ObjectTile tile = ((SceneObject)obj).tile;
                lx -= (tile.getBaseWidth() - 1);
                ry -= (tile.getBaseHeight() - 1);
            }
        }

        /**
         * Paints the dirty item to the given graphics context.  Only
         * the portion of the item that falls within the given dirty
         * rectangle is actually drawn.
         */
        public void paint (Graphics2D gfx)
        {
            if (obj instanceof Sprite) {
                ((Sprite)obj).paint(gfx);
            } else {
                ((SceneObject)obj).paint(gfx);
            }
        }

        /**
         * Returns the "depth" of our rear-most tile.
         */
        public int getRearDepth ()
        {
            return ry + lx;
        }

        /**
         * Returns the render priority for this dirty item. It will be
         * zero unless this is a display object which may have a custom
         * render priority.
         */
        public int getRenderPriority ()
        {
            if (obj instanceof SceneObject) {
                return ((SceneObject)obj).getPriority();
            } else {
                return 0;
            }
        }

        /**
         * Releases all references held by this dirty item so that it
         * doesn't inadvertently hold on to any objects while waiting to
         * be reused.
         */
        public void clear ()
        {
            obj = null;
        }

        // documentation inherited
        public boolean equals (Object other)
        {
            // we're never equal to something that's not our kind
            if (!(other instanceof DirtyItem)) {
                return false;
            }

            // sprites are equivalent if they're the same sprite
            DirtyItem b = (DirtyItem)other;
            return obj.equals(b.obj);
        }

        // documentation inherited
        public int hashCode ()
        {
            return obj.hashCode();
        }

        /**
         * Returns a string representation of the dirty item.
         */
        public String toString ()
        {
            StringBuffer buf = new StringBuffer();
            buf.append("[obj=").append(obj);
            buf.append(", ox=").append(ox);
            buf.append(", oy=").append(oy);
            buf.append(", lx=").append(lx);
            buf.append(", ly=").append(ly);
            buf.append(", rx=").append(rx);
            buf.append(", ry=").append(ry);
            return buf.append("]").toString();
        }
    }

    /**
     * A comparator class for use in sorting dirty items in ascending
     * origin x- or y-axis coordinate order.
     */
    protected static class OriginComparator implements Comparator
    {
        /**
         * Constructs an origin comparator that sorts dirty items in
         * ascending order based on their origin coordinate on the given
         * axis.
        */
        public OriginComparator (int axis)
        {
            _axis = axis;
        }

        // documentation inherited
        public int compare (Object a, Object b)
        {
            DirtyItem da = (DirtyItem)a;
            DirtyItem db = (DirtyItem)b;

            // if they don't overlap, sort them normally
            if (_axis == X_AXIS) {
                if (da.ox != db.ox) {
                    return da.ox - db.ox;
                }
            } else {
                if (da.oy != db.oy) {
                    return da.oy - db.oy;
                }
            }

            // if they do overlap, incorporate render priority; assume
            // non-display objects have a render priority of zero
            return da.getRenderPriority() - db.getRenderPriority();
        }

        /** The axis this comparator sorts on. */
        protected int _axis;
    }

    /**
     * A comparator class for use in sorting the dirty sprites and
     * objects in a scene in ascending x- and y-coordinate order
     * suitable for rendering in the isometric view with proper visual
     * results.
     */
    protected class RenderComparator implements Comparator
    {
        // documentation inherited
        public int compare (Object a, Object b)
        {
            DirtyItem da = (DirtyItem)a;
            DirtyItem db = (DirtyItem)b;

            // if the two objects are scene objects and they overlap, we
            // compare them solely based on their human assigned priority
            if ((da.obj instanceof SceneObject) &&
                (db.obj instanceof SceneObject)) {
                SceneObject soa = (SceneObject)da.obj;
                SceneObject sob = (SceneObject)db.obj;
                if (soa.objectFootprintOverlaps(sob)) {
                    int result = soa.getPriority() - sob.getPriority();
                    if (DEBUG_COMPARE) {
                        String items = DirtyItemList.toString(da, db);
                        Log.info("compare: overlapping [result=" + result +
                                 ", items=" + items + "].");
                    }
                    return result;
                }
            }

            // check for partitioning objects on the y-axis
            int result = comparePartitioned(Y_AXIS, da, db);
            if (result != 0) {
                if (DEBUG_COMPARE) {
                    String items = DirtyItemList.toString(da, db);
                    Log.info("compare: Y-partitioned " +
                             "[result=" + result + ", items=" + items + "].");
                }
                return result;
            }
            
            // check for partitioning objects on the x-axis
            result = comparePartitioned(X_AXIS, da, db);
            if (result != 0) {
                if (DEBUG_COMPARE) {
                    String items = DirtyItemList.toString(da, db);
                    Log.info("compare: X-partitioned " +
                             "[result=" + result + ", items=" + items + "].");
                }
                return result;
            }

            // use normal iso-ordering check
            result = compareNonPartitioned(da, db);
            if (DEBUG_COMPARE) {
                String items = DirtyItemList.toString(da, db);
                Log.info("compare: non-partitioned " +
                         "[result=" + result + ", items=" + items + "].");
            }

            return result;
        }

        /**
         * Returns whether two dirty items have a partitioning object
         * between them on the given axis.
         */
        protected int comparePartitioned (
            int axis, DirtyItem da, DirtyItem db)
        {
            // prepare for the partitioning check
            SortableArrayList sitems;
            Comparator comp;
            boolean swapped = false;
            switch (axis) {
            case X_AXIS:
                if (da.ox == db.ox) {
                    // can't be partitioned if there's no space between
                    return 0;
                }

                // order items for proper comparison
                if (da.ox > db.ox) {
                    DirtyItem temp = da;
                    da = db;
                    db = temp;
                    swapped = true;
                }

                // use the axis-specific sorted array
                sitems = _xitems;
                comp = ORIGIN_X_COMP;
                break;

            case Y_AXIS:
            default:
                if (da.oy == db.oy) {
                    // can't be partitioned if there's no space between
                    return 0;
                }

                // order items for proper comparison
                if (da.oy > db.oy) {
                    DirtyItem temp = da;
                    da = db;
                    db = temp;
                    swapped = true;
                }

                // use the axis-specific sorted array
                sitems = _yitems;
                comp = ORIGIN_Y_COMP;
                break;
            }

            // get the bounding item indices and the number of
            // potentially-partitioning dirty items
            int aidx = sitems.binarySearch(da, comp);
            int bidx = sitems.binarySearch(db, comp);
            int size = bidx - aidx - 1;

            // check each potentially partitioning item
            int startidx = aidx + 1, endidx = startidx + size;
            for (int pidx = startidx; pidx < endidx; pidx++) {
                DirtyItem dp = (DirtyItem)sitems.get(pidx);
                if (dp.obj instanceof Sprite) {
                    // sprites can't partition things
                    continue;
                } else if ((dp.obj == da.obj) ||
                           (dp.obj == db.obj)) {
                    // can't be partitioned by ourselves
                    continue;
                }

                // perform the actual partition check for this object
                switch (axis) {
                case X_AXIS:
                    if (dp.ly >= da.ry &&
                        dp.ry <= db.ly &&
                        dp.lx >= da.rx &&
                        dp.rx <= db.lx) {
                        return (swapped) ? 1 : -1;
                    }

                case Y_AXIS:
                default:
                    if (dp.lx <= db.ox &&
                        dp.rx >= da.lx &&
                        dp.ry >= da.oy &&
                        dp.oy <= db.ry) {
                        return (swapped) ? 1 : -1;
                    }
                }
            }

            // no partitioning object found
            return 0;
        }

        /**
         * Compares the two dirty items assuming there are no partitioning
         * objects between them.
         */
        protected int compareNonPartitioned (DirtyItem da, DirtyItem db)
        {
            if (da.ox == db.ox &&
                da.oy == db.oy) {
                if (da.equals(db)) {
                    // render level is equal if we're the same sprite
                    // or an object at the same location
                    return 0;
                }

                boolean aIsSprite = (da.obj instanceof Sprite);
                boolean bIsSprite = (db.obj instanceof Sprite);

                if (aIsSprite && bIsSprite) {
                    Sprite as = (Sprite)da.obj, bs = (Sprite)db.obj;
                    // we're comparing two sprites co-existing on the same
                    // tile, first check their render order
                    int rocomp = as.getRenderOrder() - bs.getRenderOrder();
                    if (rocomp != 0) {
                        return rocomp;
                    }
                    // next sort them by y-position
                    int ydiff = as.getY() - bs.getY();
                    if (ydiff != 0) {
                        return ydiff;
                    }
                    // if they're at the same height, just use hashCode()
                    // to establish a consistent arbitrary ordering
                    return (as.hashCode() - bs.hashCode());

                // otherwise, always put a sprite on top of a non-sprite
                } else if (aIsSprite) {
                    return 1;

                } else if (bIsSprite) {
                    return -1;
                }
            }

            // otherwise use a consistent ordering for non-overlappers;
            // see narya/docs/miso/render_sort_diagram.png for more info
            if (db.lx <= da.ox && db.ry <= da.oy) {
                return 1;
            } else if (db.rx >= da.lx && db.ly >= da.ry) {
                return -1;
            } else {
                return da.oy - db.oy;
            }
        }
    }

    /** The list of dirty items. */
    protected SortableArrayList _items = new SortableArrayList();

    /** The list of dirty items sorted by x-position. */
    protected SortableArrayList _xitems = new SortableArrayList();

    /** The list of dirty items sorted by y-position. */
    protected SortableArrayList _yitems = new SortableArrayList();

    /** The list of dirty items sorted by rear-depth. */
    protected SortableArrayList _ditems = new SortableArrayList();

    /** The render comparator we'll use for our final, magical sort. */
    protected Comparator _rcomp = new RenderComparator();

    /** Unused dirty items. */
    protected ArrayList _freelist = new ArrayList();

    /** Whether to log debug info when comparing pairs of dirty items. */
    protected static final boolean DEBUG_COMPARE = false;

    /** Whether to log debug info for the main dirty item sorting algorithm. */
    protected static final boolean DEBUG_SORT = false;

    /** Constants used to denote axis sorting constraints. */
    protected static final int X_AXIS = 0;
    protected static final int Y_AXIS = 1;

    /** The comparator used to sort dirty items in ascending origin
     * x-coordinate order. */
    protected static final Comparator ORIGIN_X_COMP =
        new OriginComparator(X_AXIS);

    /** The comparator used to sort dirty items in ascending origin
     * y-coordinate order. */
    protected static final Comparator ORIGIN_Y_COMP =
        new OriginComparator(Y_AXIS);

    /** The comparator used to sort dirty items in ascending "rear-depth"
     * order. */
    protected static final Comparator REAR_DEPTH_COMP = new Comparator() {
        public int compare (Object o1, Object o2) {
            return (((DirtyItem)o1).getRearDepth() -
                    ((DirtyItem)o2).getRearDepth());
        }
    };
}
