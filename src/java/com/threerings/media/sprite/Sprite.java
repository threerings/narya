//
// $Id: Sprite.java,v 1.2 2001/07/30 15:38:52 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.Graphics2D;

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
     * Construct a Sprite object.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param tiles the tiles used to display the sprite.
     */
    public Sprite (int x, int y, Tile[] tiles)
    {
        init(x, y, tiles);
    }

    /**
     * Construct a Sprite object without any associated tiles.  The
     * sprite should be populated with a set of tiles used to display
     * it via a subsequent call to <code>setTiles()</code>.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     */
    public Sprite (int x, int y)
    {
        init(x, y, null);
    }

    /**
     * Initialize the sprite object with the specified parameters.
     */
    protected void init (int x, int y, Tile[] tiles)
    {
        this.x = x;
        this.y = y;

        _tiles = tiles;
        _curframe = 0;
    }        

    /**
     * Paint the sprite to the specified graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        Tile tile = _tiles[_curframe];
        int xpos = x - (tile.width / 2);
        int ypos = y - tile.height;
        gfx.drawImage(tile.img, xpos, ypos, null);
    }

    /**
     * Returns whether the sprite is inside the given rectangle in
     * pixel coordinates.
     *
     * @param x the rectangle x coordinate.
     * @param y the rectangle y coordinate.
     * @param width the rectangle width.
     * @param height the rectangle height.
     *
     * @return true if the sprite is inside the rectangle, false if not.
     */
    public boolean inside (int x, int y, int width, int height)
    {
        // treat the sprite as having a width and height of 1 pixel for now
        return (this.x >= x && this.x <= (x + width) &&
                this.y >= y && this.y <= (y + height));
    }

    /**
     * Set the tile array used to render the sprite.
     *
     * @param tiles the sprite tiles.
     */
    public void setTiles (Tile[] tiles)
    {
        _tiles = tiles;
    }

    /** The tiles used to render the sprite. */
    protected Tile[] _tiles;

    /** The current tile index to render. */
    protected int _curframe;
}
