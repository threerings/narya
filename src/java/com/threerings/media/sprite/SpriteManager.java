//
// $Id: SpriteManager.java,v 1.10 2001/08/22 02:14:57 mdb Exp $

package com.threerings.media.sprite;

import java.awt.*;
import java.util.ArrayList;

import com.threerings.media.Log;

/**
 * The SpriteManager manages the sprites running about in the game.
 */
public class SpriteManager
{
    /**
     * Construct and initialize the SpriteManager object.
     */
    public SpriteManager ()
    {
        _sprites = new ArrayList();
        _dirty = new DirtyRectList();
    }

    /**
     * Add a rectangle to the dirty rectangle list.
     *
     * @param rect the rectangle to add.
     */
    public void addDirtyRect (Rectangle rect)
    {
        _dirty.add(rect);
    }

    /**
     * When an animated view processes its dirty rectangles, it may
     * require an expansion of the dirty region which may in turn require
     * the invalidation of more sprites than were originally invalid. In
     * such cases, the animated view can call back to the sprite manager,
     * asking it to append the rectangles of the sprites that intersect a
     * particular region to the dirty rectangle list that it's processing.
     * A sprite's rectangle will only be appended if it's not already in
     * the list.
     *
     * @param rects the dirty rectangle list being processed by the
     * animated view.
     * @param bounds the bounds the intersection of which we have
     * interest.
     */
    public void invalidateIntersectingSprites (
        DirtyRectList rects, Polygon bounds)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.intersects(bounds)) {
                if (rects.appendDirtyRect(sprite.getRenderedBounds())) {
                    Log.info("Expanded for: " + sprite);
                }
            }
        }
    }

    /**
     * Add a sprite to the set of sprites managed by this manager.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite (Sprite sprite)
    {
        // let the sprite know about us
        sprite.setSpriteManager(this);
        // add the sprite to our list
        _sprites.add(sprite);
        // and invalidate the sprite's original position
        sprite.invalidate();
    }

    /**
     * Removes the specified sprite from the set of sprites managed by
     * this manager.
     *
     * @param sprite the sprite to remove.
     */
    public void removeSprite (Sprite sprite)
    {
        // invalidate the current sprite position
        sprite.invalidate();
        // remove the sprite from our list
        _sprites.remove(sprite);
        // clear out our manager reference
        sprite.setSpriteManager(null);
    }

    /**
     * Return the list of dirty rects in screen pixel coordinates that
     * have been created by any sprites since the last time the dirty
     * rects were requested.
     *
     * @return the list of dirty rects.
     */
    public DirtyRectList getDirtyRects ()
    {
        // create a copy of the dirty rectangles
        DirtyRectList dirty = (DirtyRectList)_dirty.clone();

        // clear out the list
        _dirty.clear();

        // return the full original list
        return dirty;
    }

    /**
     * Render the sprites residing within the given polygon to the given
     * graphics context.
     *
     * @param gfx the graphics context.
     * @param bounds the bounding polygon.
     */
    public void renderSprites (Graphics2D gfx, Polygon bounds)
    {
        // TODO: optimize to store sprites based on quadrants they're
        // in (or somesuch), and sorted, so that we can more quickly
        // determine which sprites to draw.

        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.inside(bounds)) {
                sprite.paint(gfx);
            }
        }
    }

    /**
     * Render the sprite paths to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    public void renderSpritePaths (Graphics2D gfx)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    sprite.paintPath(gfx);
        }
    }

    /**
     * Call <code>tick()</code> on all sprite objects to give them a
     * chance to move themselves about, change their display image,
     * and so forth.
     */
    public void tick ()
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            sprite.tick();
        }
    }

    /** The sprite objects we're managing. */
    protected ArrayList _sprites;

    /** The dirty rectangles created by sprites. */
    protected DirtyRectList _dirty;
}
