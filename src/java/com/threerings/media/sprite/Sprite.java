//
// $Id: Sprite.java,v 1.9 2001/08/14 22:54:45 mdb Exp $

package com.threerings.media.sprite;

import java.awt.*;
import java.util.Enumeration;

import com.threerings.media.Log;
import com.threerings.media.util.MathUtil;

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
        _animDelay = ANIM_NONE;
        _numTicks = 0;

        setTiles(_tiles);

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
     * Set the sprite's active path and start moving it along its
     * merry way.  If the sprite is already moving along a previous
     * path the old path will be lost and the new path will begin to
     * be traversed.
     *
     * @param path the path to follow.
     */
    public void move (Path path)
    {
        // make sure following the path is a sensible thing to do
        if (path == null || path.size() < 2) return;

        // save an enumeration of the path nodes
        _path = path.elements();

        // skip the first node since it's our starting position.
        // perhaps someday we'll do something with this.
        _path.nextElement();

        // start our meandering
        moveAlongPath();
    }

    /**
     * Start the sprite moving toward the next node in its path.
     */
    protected void moveAlongPath ()
    {
        // grab the next node in our path
        _dest = (PathNode)_path.nextElement();

        // if no more nodes remain, clear out our path and bail
        if (_dest == null) {
            _path = null;
            return;
        }

        Log.info("moveAlongPath [dest=" + _dest + "].");

	// if we're already here, move on to the next node
	if (x == _dest.loc.x && y == _dest.loc.y) {
	    moveAlongPath();
	    return;
	}

        // determine the horizontal/vertical move increments
        float dist = MathUtil.distance(x, y, _dest.loc.x, _dest.loc.y);
        _incx = (float)(_dest.loc.x - x) / dist;
        _incy = (float)(_dest.loc.y - y) / dist;

        // init position data used to track fractional pixels
        _movex = x;
        _movey = y;
    }

    /**
     * Invalidate the sprite's display rectangle for later repainting.
     */
    public void invalidate ()
    {
        if (_curTile == null) return;

        Rectangle dirty = new Rectangle(
            _drawx, _drawy, _curTile.width, _curTile.height);

//  	Log.info("Sprite invalidate [x=" + x + ", y=" + y +
//  		 ", dx=" + dirty.x + ", dy=" + dirty.y +
//  		 ", dwidth=" + dirty.width +
//  		 ", dheight=" + dirty.height + "].");

        _spritemgr.addDirtyRect(dirty);
    }

    /**
     * This method is called periodically by the SpriteManager to give
     * the sprite a chance to update its state. 
     */
    public void tick ()
    {
        // increment the display tile if performing tile animation
        if (_animDelay != ANIM_NONE && (_numTicks++ == _animDelay)) {
            _numTicks = 0;
            if (++_curFrame > _tiles.length - 1) _curFrame = 0;
            _curTile = _tiles[_curFrame];

            // dirty our rectangle since we've altered our display tile
            invalidate();
        }

        // move the sprite along toward its destination, if any 
        if (_dest != null) {
            handleMove();
        }
    }

    /**
     * Actually move the sprite's position toward its destination one
     * display increment.
     */
    protected void handleMove ()
    {
	// dirty our rectangle since we're going to move
	invalidate();

        // move the sprite incrementally toward its goal
        x = (int)(_movex += _incx);
        y = (int)(_movey += _incy);

        // stop moving once we've reached our destination
        if (_incx > 0 && x > _dest.loc.x || _incx < 0 && x < _dest.loc.x ||
            _incy > 0 && y > _dest.loc.y || _incy < 0 && y < _dest.loc.y) {

            // make sure we stop exactly where desired
            x = _dest.loc.x;
            y = _dest.loc.y;

            // move further along the path if necessary
            moveAlongPath();
        }

	// update the draw coordinates to reflect our new position
        updateDrawPosition();

	// dirty our rectangle in the new position
	invalidate();
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

    /** Value used to denote that no tile animation is desired. */
    protected static final int ANIM_NONE = -1;

    /** The tiles used to render the sprite. */
    protected Tile[] _tiles;

    /** The current tile to render the sprite. */
    protected Tile _curTile;

    /** The current tile index to render. */
    protected int _curFrame;

    /** The coordinates at which the tile image is drawn. */
    protected int _drawx, _drawy;

    /** The PathNode objects describing the path the sprite is following. */
    protected Enumeration _path;

    /** When moving, the sprite's destination path node. */
    protected PathNode _dest;

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
