//
// $Id: SpriteManager.java,v 1.8 2001/08/21 00:58:36 mdb Exp $

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
        _dirty = new ArrayList();
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
     * Add a sprite to the set of sprites managed by this manager.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite (Sprite sprite)
    {
        _sprites.add(sprite);
    }

    /**
     * Removes the specified sprite from the set of sprites managed by
     * this manager.
     *
     * @param sprite the sprite to remove.
     */
    public void removeSprite (Sprite sprite)
    {
        _sprites.remove(sprite);
    }

    /**
     * Return the list of dirty rects in screen pixel coordinates that
     * have been created by any sprites since the last time the dirty
     * rects were requested.
     *
     * @return the list of dirty rects.
     */
    public ArrayList getDirtyRects ()
    {
        // create a copy of the dirty rectangles
        ArrayList dirty = (ArrayList)_dirty.clone();

        // clear out the list
        _dirty.clear();

        // return the full original list
        return dirty;
    }

    /**
     * Render the sprites residing within the given polygon to the
     * given graphics context.
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
    protected ArrayList _dirty;
}
