//
// $Id: DirtyItemList.java,v 1.6 2001/11/18 04:09:22 mdb Exp $

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
    /**
     * Appends the dirty sprite at the given coordinates to the dirty
     * item list.
     */
    public void appendDirtySprite (
        MisoCharacterSprite sprite, int x, int y, Rectangle dirtyRect)
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
        Arrays.sort(items, DIRTY_COMP);
        return items;
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

        /** The bounds of the dirty item if it's an object tile. */
        public Shape bounds;

        /** The origin tile coordinates. */
        public int ox, oy;

        /** The leftmost tile coordinates. */
        public int lx, ly;

        /** The rightmost tile coordinates. */
        public int rx, ry;

        /** The dirty rectangle. */
        public Rectangle dirtyRect;

        /**
         * Constructs a dirty item.
         */
        public DirtyItem (
            Object obj, Shape bounds, int x, int y, Rectangle dirtyRect)
        {
            this.obj = obj;
            this.bounds = bounds;
            this.ox = x;
            this.oy = y;
            this.dirtyRect = dirtyRect;

            // calculate the item's leftmost and rightmost tiles.
            // note that sprites occupy only a single tile, so
            // leftmost and rightmost tiles are equivalent
            lx = rx = ox;
            ly = ry = oy;
            if (obj instanceof ObjectTile) {
                ObjectTile tile = (ObjectTile)obj;
                lx -= (tile.getBaseWidth() - 1);
                ry -= (tile.getBaseHeight() - 1);
            }
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
            // simply by origin tile coordinate since they can never
            // occupy the same tile
            return (ox == b.ox && oy == b.oy);
        }

        public String toString ()
        {
            return "[obj=" + obj + ", ox=" + ox + ", oy=" + oy +
                ", lx=" + lx + ", ly=" + ly + ", rx=" + rx +
                ", ry=" + ry + "]";
        }
    }

    /** The dirty item comparator used to sort dirty items back to front. */
    protected static final Comparator DIRTY_COMP = new DirtyItemComparator();

    /**
     * A comparator class for use in sorting the dirty sprites and
     * objects in a scene in ascending x- and y-coordinate order
     * suitable for rendering in the isometric view with proper visual
     * results.
     */
    protected static class DirtyItemComparator implements Comparator
    {
        public int compare (Object a, Object b)
        {
            DirtyItem da = (DirtyItem)a;
            DirtyItem db = (DirtyItem)b;

            if (da.ox == db.ox &&
                da.oy == db.oy) {

                if (da.equals(db)) {
                    // render level is equal if we're the same sprite
                    // or an object at the same location
                    return 0;
                }

                if ((da.obj instanceof MisoCharacterSprite) &&
                    (db.obj instanceof MisoCharacterSprite)) {
                    // we're comparing two sprites co-existing on the same
                    // tile, so study their fine coordinates to determine
                    // rendering order
                    MisoCharacterSprite as = (MisoCharacterSprite)da.obj;
                    MisoCharacterSprite bs = (MisoCharacterSprite)db.obj;

                    int ahei = as.getFineX() + as.getFineY();
                    int bhei = bs.getFineX() + bs.getFineY();

                    if (ahei < bhei) {
                        // item b is in front of item a
                        return -1;
                    } else if (ahei > bhei) {
                        // item a is in front of item b
                        return 1;
                    } else {
                        // if they're at the same vertical row of
                        // intra-tile tiles, just use something consistent
                        return as.hashCode() - bs.hashCode();
                    }
                }
            }

            if (da.lx > db.ox ||
                da.ry > db.oy) {
                // item a is in front of item b
                return 1;
            }

            // item b is in front of item a
            return -1;
        }

        public boolean equals (Object obj)
        {
	    return (obj == this);
        }
    }
}
