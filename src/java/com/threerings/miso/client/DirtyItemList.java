//
// $Id: DirtyItemList.java,v 1.18 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.ObjectTile;
import com.threerings.miso.client.util.IsoUtil;

/**
 * The dirty item list keeps track of dirty sprites and object tiles
 * in a scene.
 */
public class DirtyItemList
{
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
        item.init(sprite, null, null, tx, ty);
        _items.add(item);
    }

    /**
     * Appends the dirty object tile at the given coordinates to the dirty
     * item list.
     *
     * @param scene the scene object that is dirty.
     * @param footprint the footprint of the object tile if it should be
     * rendered, null otherwise.
     */
    public void appendDirtyObject (DisplayObjectInfo info, Shape footprint)
    {
        DirtyItem item = getDirtyItem();
        item.init(info, info.bounds, footprint, info.x, info.y);
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

            // sort items into proper render order
            _items.sort(_rcomp);

            // clear out our temporary arrays
            _xitems.clear();
            _yitems.clear();
        }

        if (DEBUG_SORT) {
            Log.info("Sorted for render [items=" + toString(_items) + "].");
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
     * Returns an abbreviated string representation of the two given dirty
     * items describing each by only its origin coordinates.  Intended for
     * debugging purposes.
     */
    protected static String toString (DirtyItem a, DirtyItem b)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[(ox=").append(a.ox);
        buf.append(", oy=").append(a.oy).append("), ");
        buf.append("(ox=").append(b.ox);
        buf.append(", oy=").append(b.oy).append(")");
        return buf.append("]").toString();
    }

    /**
     * Returns an abbreviated string representation of the given dirty
     * items describing each by only its origin coordinates.  Intended for
     * debugging purposes.
     */
    protected static String toString (SortableArrayList items)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int ii = 0; ii < items.size(); ii++) {
            DirtyItem item = (DirtyItem)items.get(ii);
            buf.append("(ox=").append(item.ox);
            buf.append(", oy=").append(item.oy).append(")");
            if (ii < (items.size() - 1)) {
                buf.append(", ");
            }
        }
        return buf.append("]").toString();
    }

    /**
     * A class to hold the items inserted in the dirty list along with
     * all of the information necessary to render their dirty regions
     * to the target graphics context when the time comes to do so.
     */
    public static class DirtyItem
    {
        /** The dirtied object; one of either a sprite or an object tile. */
        public Object obj;

        /** The bounds of the dirty item if it's an object tile. */
        public Rectangle bounds;

        /** The footprint of the dirty item if it's an object tile and
         * we're drawing footprints. */
        public Shape footprint;

        /** The origin tile coordinates. */
        public int ox, oy;

        /** The leftmost tile coordinates. */
        public int lx, ly;

        /** The rightmost tile coordinates. */
        public int rx, ry;

        /**
         * Initializes a dirty item.
         */
        public void init (Object obj, Rectangle bounds, Shape footprint,
                          int x, int y)
        {
            this.obj = obj;
            this.bounds = bounds;
            this.footprint = footprint;
            this.ox = x;
            this.oy = y;

            // calculate the item's leftmost and rightmost tiles; note
            // that sprites occupy only a single tile, so leftmost and
            // rightmost tiles are equivalent
            lx = rx = ox;
            ly = ry = oy;
            if (obj instanceof DisplayObjectInfo) {
                ObjectTile tile = ((DisplayObjectInfo)obj).tile;
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
            // if there's a footprint, paint that
            if (footprint != null) {
                gfx.setColor(Color.black);
                gfx.draw(footprint);
            }

            // paint the item
            if (obj instanceof Sprite) {
                ((Sprite)obj).paint(gfx);
            } else {
                ((DisplayObjectInfo)obj).tile.paint(gfx, bounds.x, bounds.y);
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
            bounds = null;
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
//             if ((obj instanceof Sprite) && (b.obj instanceof Sprite)) {
//                 return (obj == b.obj);
//             }

//             // objects are equivalent if they are the same object
//             if ((obj instanceof DisplayObjectInfo) && (b.obj instanceof DisplayObjectInfo)) {
//                 return (obj == b.obj);
//             }

//             // object-to-sprite are distinguished simply by origin tile
//             // coordinate since they can never occupy the same tile
//             return (ox == b.ox && oy == b.oy);
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
            return (_axis == X_AXIS) ? (da.ox - db.ox) : (da.oy - db.oy);
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
                        dp.lx > da.rx &&
                        dp.rx < db.lx) {
                        return (swapped) ? 1 : -1;
                    }

                case Y_AXIS:
                default:
                    if (dp.lx <= db.ox &&
                        dp.rx >= da.lx &&
                        dp.ry > da.oy &&
                        dp.oy < db.ry) {
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

            // if the two objects are scene objects and they overlap, we
            // compare them solely based on their human assigned render
            // priority scene; this allows us to avoid all sorts of sticky
            // business wherein the render order between two overlapping
            // objects cannot be determined without a z-buffer
            if ((da.obj instanceof DisplayObjectInfo) &&
                (db.obj instanceof DisplayObjectInfo)) {
                DisplayObjectInfo soa = (DisplayObjectInfo)da.obj;
                DisplayObjectInfo sob = (DisplayObjectInfo)db.obj;
                if (IsoUtil.objectFootprintsOverlap(soa, sob)) {
                    return (soa.priority - sob.priority);
                }
            }

            // otherwise use a consistent ordering for non-overlappers;
            // see narya/docs/miso/render_sort_diagram.png for more info
            if (da.lx > db.ox) {
                return 1;
            } else if (da.ry > db.oy) {
                return (db.lx > da.ox) ? -1 : 1;
            }
            return -1;
        }
    }

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

    /** The list of dirty items. */
    protected SortableArrayList _items = new SortableArrayList();

    /** The list of dirty items sorted by x-position. */
    protected SortableArrayList _xitems = new SortableArrayList();

    /** The list of dirty items sorted by y-position. */
    protected SortableArrayList _yitems = new SortableArrayList();

    /** The render comparator we'll use for our final, magical sort. */
    protected Comparator _rcomp = new RenderComparator();

    /** Unused dirty items. */
    protected ArrayList _freelist = new ArrayList();
}
