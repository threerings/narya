//
// $Id: SpriteManager.java,v 1.5 2001/08/02 18:59:00 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.*;
import java.util.ArrayList;

import com.threerings.miso.Log;

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
     * Add a sprite to the set of sprites managed by this SpriteManager.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite (Sprite sprite)
    {
        _sprites.add(sprite);
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
     * Start a sprite moving along a particular path.  The sprite will
     * continue to be moved along the path until the final destination
     * is reached, or until the sprite is brought to a halt by some
     * has-yet-to-be-determined means.
     *
     * @param sprite the sprite to move.
     * @param path the path to move the sprite along.
     */
    public void moveSprite (Sprite sprite, Path path)
    {
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
