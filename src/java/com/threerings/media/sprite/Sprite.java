//
// $Id: Sprite.java,v 1.11 2001/08/15 22:06:21 shaper Exp $

package com.threerings.media.sprite;

import java.awt.*;
import java.util.Enumeration;

import com.threerings.media.Log;
import com.threerings.media.util.MathUtil;

/**
 * The <code>Sprite</code> class represents a single moveable object
 * within a scene.  A sprite has a position within the scene, and a set of
 * images used to render it (perhaps multiple frames for animation).
 */
public class Sprite
{
    /** The sprite's x-position in pixel coordinates. */
    public int x;

    /** The sprite's y-position in pixel coordinates. */
    public int y;

    /**
     * Construct a sprite object.
     *
     * @param spritemgr the sprite manager.
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param frames the multi-frame image used to display the sprite.
     */
    public Sprite (SpriteManager spritemgr, int x, int y,
                   MultiFrameImage frames)
    {
        init(spritemgr, x, y, frames);
    }

    /**
     * Construct a sprite object without any associated frames. The sprite
     * should be populated with a set of frames used to display it via a
     * subsequent call to <code>setFrames()</code>.
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
    protected void init (
        SpriteManager spritemgr, int x, int y, MultiFrameImage frames)
    {
        _spritemgr = spritemgr;

        this.x = x;
        this.y = y;

        updateDrawPosition();

        _frameIdx = 0;
        _animDelay = ANIM_NONE;
        _numTicks = 0;

        setFrames(frames);

        invalidate();
    }        

    /**
     * Paint the sprite to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        gfx.drawImage(_frame, _drawx, _drawy, null);
    }

    /**
     * Paint the sprite's path, if any, to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paintPath (Graphics2D gfx)
    {
	if (_fullpath == null) return;

	gfx.setColor(Color.red);
	Point prev = null;
	int size = _fullpath.size();
	for (int ii = 0; ii < size; ii++) {
	    PathNode n = (PathNode)_fullpath.getNode(ii);
	    if (prev == null) prev = n.loc;
	    gfx.drawLine(prev.x, prev.y, n.loc.x, n.loc.y);
	    prev = n.loc;
	}
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
     * Set the number of ticks to wait before switching to the next image
     * in the array of images used to display the sprite.
     *
     * @param ticks the number of ticks.
     */
    public void setAnimationDelay (int ticks)
    {
        _animDelay = ticks;
    }

    /**
     * Set the image array used to render the sprite.
     *
     * @param frames the sprite images.
     */
    public void setFrames (MultiFrameImage frames)
    {
        if (frames == null) return;

        _frames = frames;
        _frame = _frames.getFrame(_frameIdx);
        updateDrawPosition();
        invalidate();
    }

    /**
     * Stop the sprite from any movement along a path it may be
     * engaged in.
     */
    public void stop ()
    {
	// TODO: make sure we come to a stop on a full coordinate,
	// even in the case where we aborted a path walk mid-traversal.
	_dest = null;
	_path = null;
	_fullpath = null;
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
        if (path == null || path.size() < 2) {
	    // halt movement if we're walking since, regardless of its
	    // reasonableness, we've been asked to follow a new path
	    stop();
	    return;
	}

        // save an enumeration of the path nodes
        _path = path.elements();

	// and the full path for potential rendering
	_fullpath = path;

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
	    stop();
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
        if (_frame == null) return;

        Rectangle dirty = new Rectangle(
            _drawx, _drawy, _frame.getWidth(null), _frame.getHeight(null));

        _spritemgr.addDirtyRect(dirty);
    }

    /**
     * This method is called periodically by the SpriteManager to give
     * the sprite a chance to update its state. 
     */
    public void tick ()
    {
        // increment the display image if performing image animation
        if (_animDelay != ANIM_NONE && (_numTicks++ == _animDelay)) {
            _numTicks = 0;
            _frameIdx = (_frameIdx + 1) % _frames.getFrameCount();
            _frame = _frames.getFrame(_frameIdx);

            // dirty our rectangle since we've altered our display image
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
        if (_frame == null) {
            _drawx = x;
            _drawy = y;

        } else {
            _drawx = x - (_frame.getWidth(null) / 2);
            _drawy = y - _frame.getHeight(null);
        }
    }

    /**
     * Return a string representation of the sprite.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[x=").append(x);
        buf.append(", y=").append(y);
        buf.append(", fidx=").append(_frameIdx);
        return buf.append("]").toString();
    }

    /** Value used to denote that no image animation is desired. */
    protected static final int ANIM_NONE = -1;

    /** The images used to render the sprite. */
    protected MultiFrameImage _frames;

    /** The current frame being rendered. */
    protected Image _frame;

    /** The current frame index to render. */
    protected int _frameIdx;

    /** The coordinates at which the frame image is drawn. */
    protected int _drawx, _drawy;

    /** The PathNode objects describing the path the sprite is following. */
    protected Enumeration _path;

    /** When moving, the sprite's destination path node. */
    protected PathNode _dest;

    /** When moving, the sprite position including fractional pixels. */ 
    protected float _movex, _movey;

    /** When moving, the distance to move per tick in fractional pixels. */
    protected float _incx, _incy;

    /** The number of ticks to wait before rendering with the next image. */
    protected int _animDelay;

    /** The number of ticks since the last image animation. */
    protected int _numTicks;

    /** When moving, the full path the sprite is traversing. */
    protected Path _fullpath;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
