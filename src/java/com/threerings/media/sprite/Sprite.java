//
// $Id: Sprite.java,v 1.1 2001/07/28 01:50:07 shaper Exp $

package com.threerings.miso.sprite;

import com.threerings.miso.tile.Tile;

/**
 * The Sprite class represents a single moveable object within a
 * scene.  A sprite has a position within the scene, and a set of
 * tiles used to render it (perhaps multiple frames for animation).
 */
public class Sprite
{
    /** The sprite's x-position in pixel coordinates. */
    public int x;

    /** The sprite's y-position in pixel coordinates. */
    public int y;

    /**
     * Construct and initialize a Sprite object.
     */
    public Sprite (int x, int y, Tile[] tiles)
    {
        this.x = x;
        this.y = y;

        _tiles = tiles;
        _curframe = 0;
    }

    /** The tiles that comprise the sprite. */
    protected Tile[] _tiles;

    /** The current tile frame to render. */
    protected int _curframe;
}
