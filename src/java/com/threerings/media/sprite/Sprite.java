//
// $Id: Sprite.java,v 1.5 2001/08/02 18:59:00 shaper Exp $

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
     * @param spritemgr the sprite manager.
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param tiles the tiles used to display the sprite.
     */
    public Sprite (SpriteManager spritemgr, int x, int y, Tile[] tiles)
    {
        init(spritemgr, x, y, tiles);
    }

    /**
     * Construct a Sprite object without any associated tiles.  The
     * sprite should be populated with a set of tiles used to display
     * it via a subsequent call to <code>setTiles()</code>.
     *
     * @param spritemgr the sprite manager.
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     */
    public Sprite (SpriteManager spritemgr, int x, int y)
    {
        init(spritemgr, x, y, null);
    }

    /**
     * Initialize the sprite object with its variegated parameters.
     */
    protected void init (SpriteManager spritemgr, int x, int y, Tile[] tiles)
    {
        _spritemgr = spritemgr;

        this.x = x;
        this.y = y;

        updateDrawPosition();

        _curFrame = 0;
        _animDelay = -1;
        _numTicks = 0;

        setTiles(_tiles);

        _dest = new Point();
        _state = STATE_NONE;

        invalidate();
    }        

    /**
     * Paint the sprite to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        gfx.drawImage(_curTile.img, _drawx, _drawy, null);
//          Log.info("Sprite painting image [sprite=" + this + "].");
    }

    /**
     * Returns whether the sprite is inside the given polygon in
     * pixel coordinates.
     *
     * @param bounds the bounding polygon.
     *
     * @return whether the sprite is inside the polygon.
     */
    public boolean inside (Polygon bounds)
    {
        return bounds.contains(this.x, this.y);
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
        if (tiles == null) return;

        _tiles = tiles;
        _curTile = _tiles[_curFrame];
        updateDrawPosition();
        invalidate();
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
     * Invalidate the sprite's display rectangle for later repainting.
     */
    public void invalidate ()
    {
        if (_curTile == null) return;

        int xpos = x - (_curTile.width / 2);
        int ypos = y - _curTile.height;

        Rectangle dirty =
            new Rectangle(xpos, ypos, _curTile.width, _curTile.height);

//          Log.info("Sprite dirtying rect [x=" + dirty.x + ", y=" + dirty.y +
//                   ", width=" + dirty.width + ", height=" + dirty.height + "].");

        _spritemgr.addDirtyRect(dirty);
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
            invalidate();
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

                // invalidate the sprite in its new location
                invalidate();

                // note our stoppage
                _animDelay = -1;
                _state = STATE_NONE;
            }

            updateDrawPosition();
            break;
        }
    }

    /**
     * Update the coordinates at which the sprite image is drawn to
     * reflect the sprite's current position.
     */
    protected void updateDrawPosition ()
    {
        if (_curTile == null) {
            _drawx = x;
            _drawy = y;
            return;
        }

        _drawx = x - (_curTile.width / 2);
        _drawy = y - _curTile.height;
    }

    /**
     * Return a string representation of the sprite.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[x=").append(x);
        buf.append(", y=").append(y);
        buf.append(", curframe=").append(_curFrame);
        return buf.append("]").toString();
    }

    /** State constants. */
    protected static final int STATE_NONE = 0;
    protected static final int STATE_MOVING = 1;

    /** The tiles used to render the sprite. */
    protected Tile[] _tiles;

    /** The current tile to render the sprite. */
    protected Tile _curTile;

    /** The current tile index to render. */
    protected int _curFrame;

    /** The coordinates at which the tile image is drawn. */
    protected int _drawx, _drawy;

    /** The sprite's current state. */
    protected int _state;

    /** When moving, the sprite's destination coordinates. */
    protected Point _dest;

    /** When moving, the sprite position including fractional pixels. */ 
    protected float _movex, _movey;

    /** When moving, the distance to move per tick in fractional pixels. */
    protected float _incx, _incy;

    /** The number of ticks to wait before rendering with the next tile. */
    protected int _animDelay;

    /** The number of ticks since the last tile animation. */
    protected int _numTicks;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
