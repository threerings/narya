//
// $Id: DirtyItemList.java,v 1.3 2001/10/19 23:26:31 shaper Exp $

package com.threerings.media.sprite;

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
         * the dirty rectangle of the item is question is actually
         * drawn.
         */
        public void paint (Graphics2D gfx)
        {
            // clip the draw region to the dirty portion of the item
            gfx.setClip(dirtyRect);

            // paint the item
            if (obj instanceof Sprite) {
                ((Sprite)obj).paint(gfx);
            } else {
                ((ObjectTile)obj).paint(gfx, bounds);
            }
        }
    }
}
