//
// $Id: Sprite.java,v 1.3 2001/07/31 01:38:28 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.*;

import com.threerings.miso.Log;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.util.MathUtil;

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

        _curFrame = 0;
        _animDelay = -1;
        _numTicks = 0;
        setTiles(_tiles);

        _dest = new Point();
        _state = STATE_NONE;
    }        

    /**
     * Paint the sprite to the specified graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        int xpos = x - (_curTile.width / 2);
        int ypos = y - _curTile.height;
        gfx.drawImage(_curTile.img, xpos, ypos, null);
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
        return (this.x >= x && this.x < (x + width) &&
                this.y >= y && this.y < (y + height));
    }

    /**
     * Set the number of ticks to wait before switching to the next
     * tile in the array of tiles used to display the sprite.
     *
     * @param ticks the number of ticks.
     */
    public void setAnimationDelay (int ticks)
    {
        _animDelay = ticks;
    }

    /**
     * Set the tile array used to render the sprite.
     *
     * @param tiles the sprite tiles.
     */
    public void setTiles (Tile[] tiles)
    {
        _tiles = tiles;
        if (_tiles != null) {
            _curTile = _tiles[_curFrame];
        }
    }

    /**
     * Set the destination of the sprite in pixel coordinates.
     *
     * @param x the destination x-position.
     * @param y the destination y-position.
     */
    public void setDestination (int x, int y)
    {
        // bail if we're already there
        if (x == this.x && y == this.y) return;

        // note our destination
        _dest.setLocation(x, y);

        // determine the horizontal/vertical move increments
        float dist = MathUtil.distance(this.x, this.y, x, y);
        _incx = (float)(x - this.x) / dist;
        _incy = (float)(y - this.y) / dist;

        // init position data used to track fractional pixels
        _movex = this.x;
        _movey = this.y;

        // and that we're moving toward it
        _state = STATE_MOVING;
    }

    /**
     * This method is called periodically by the SpriteManager to give
     * the sprite a chance to update its state. 
     */
    public void tick ()
    {
        // increment the display tile if performing tile animation
        if (_animDelay != -1 && (_numTicks++ == _animDelay)) {
            _numTicks = 0;
            if (++_curFrame > _tiles.length - 1) _curFrame = 0;
            _curTile = _tiles[_curFrame];
        }

        switch (_state) {
        case STATE_MOVING:
            // move the sprite incrementally toward its goal
            x = (int)(_movex += _incx);
            y = (int)(_movey += _incy);

            // stop moving once we've reached our destination
            if (_incx > 0 && x > _dest.x || _incx < 0 && x < _dest.x ||
                _incy > 0 && y > _dest.y || _incy < 0 && y < _dest.y) {

                // make sure we stop exactly where desired
                x = _dest.x;
                y = _dest.y;

                // and note our stoppage
                _animDelay = -1;
                _state = STATE_NONE;
            }
            break;
        }
    }

    // State constants. 
    protected static final int STATE_NONE = 0;
    protected static final int STATE_MOVING = 1;

    /** The tiles used to render the sprite. */
    protected Tile[] _tiles;

    /** The current tile to render the sprite. */
    protected Tile _curTile;

    /** The current tile index to render. */
    protected int _curFrame;

    /** The sprite's destination coordinates. */
    protected Point _dest;

    /** The sprite's current state. */
    protected int _state;

    /** The sprite position with fractional pixels while moving. */ 
    protected float _movex, _movey;

    /** The distance to move the sprite per tick in fractional pixels. */
    protected float _incx, _incy;

    /** The number of ticks to wait before proceeding to the next tile. */
    protected int _animDelay;

    /** The number of ticks since the last tile animation. */
    protected int _numTicks;
}
