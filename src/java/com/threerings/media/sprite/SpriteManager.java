//
// $Id: SpriteManager.java,v 1.3 2001/07/31 01:38:28 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.Graphics2D;
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
     * Render the sprites residing within the specified pixel bounds
     * to the given graphics context.
     *
     * @param gfx the graphics context.
     * @param x the bounds x-position.
     * @param y the bounds y-position.
     * @param width the bounds width.
     * @param height the bounds height.
     */
    public void renderSprites (
        Graphics2D gfx, int x, int y, int width, int height)
    {
        // TODO: optimize to store sprites based on quadrants they're
        // in (or somesuch), and sorted, so that we can more quickly
        // determine which sprites to draw.

        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.inside(x, y, width, height)) {
                sprite.paint(gfx);
            }
        }
    }

    /**
     * Call <code>Sprite.tick()</code> on all sprite objects to give
     * them a chance to move themselves about, change their display
     * image, and so forth.
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
}
