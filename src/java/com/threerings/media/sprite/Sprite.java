//
// $Id: Sprite.java,v 1.28 2001/10/25 01:39:38 shaper Exp $

package com.threerings.media.sprite;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.media.Log;

/**
 * The sprite class represents a single moveable object in an animated
 * view.  A sprite has a position within the view, and a set of images
 * used to render it (perhaps multiple frames for animation).
 */
public class Sprite
{
    /** The number of distinct directions. */
    public static final int NUM_DIRECTIONS = 8;

    /** Direction constants. */
    public static final int DIR_NONE = -1;
    public static final int DIR_SOUTHWEST = 0;
    public static final int DIR_WEST = 1;
    public static final int DIR_NORTHWEST = 2;
    public static final int DIR_NORTH = 3;
    public static final int DIR_NORTHEAST = 4;
    public static final int DIR_EAST = 5;
    public static final int DIR_SOUTHEAST = 6;
    public static final int DIR_SOUTH = 7;

    /** String translations for the direction constants. */
    public static String[] XLATE_DIRS = {
	"Southwest", "West", "Northwest", "North", "Northeast",
	"East", "Southeast", "South"
    };

    /** Default frame rate. */
    public static final int DEFAULT_FRAME_RATE = 15;

    /** Animation mode indicating no animation. */
    public static final int NO_ANIMATION = 0;

    /** Animation mode indicating movement cued animation. */
    public static final int MOVEMENT_CUED = 1;

    /** Animation mode indicating time based animation. */
    public static final int TIME_BASED = 2;

    /**
     * Construct a sprite object.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param frames the multi-frame image used to display the sprite.
     */
    public Sprite (int x, int y, MultiFrameImage frames)
    {
        init(x, y, frames);
    }

    /**
     * Construct a sprite object without any associated frames. The sprite
     * should be populated with a set of frames used to display it via a
     * subsequent call to {@link #setFrames}.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     */
    public Sprite (int x, int y)
    {
        init(x, y, null);
    }

    /**
     * Returns the sprite's x position in screen coordinates.
     */
    public int getX ()
    {
        return _x;
    }

    /**
     * Returns the sprite's y position in screen coordinates.
     */
    public int getY ()
    {
        return _y;
    }

    /**
     * Returns the sprite's width in pixels.
     */
    public int getWidth ()
    {
	return _bounds.width;
    }

    /**
     * Returns the sprite's height in pixels.
     */
    public int getHeight ()
    {
	return _bounds.height;
    }

    /**
     * Returns the sprite's rendered bounds in pixels.
     */
    public Rectangle getBounds ()
    {
	return _bounds;
    }

    /**
     * Moves the sprite to the specified location.
     *
     * @param x the x-position in pixels.
     * @param y the y-position in pixels.
     */
    public void setLocation (int x, int y)
    {
        // create a starting dirty rectangle with our current position
        Rectangle dirty = new Rectangle(_bounds);
        // move ourselves
        _x = x;
        _y = y;
        // we need to update our draw position which is based on the size
        // of our current frame
        updateRenderOrigin();
        // grow the dirty rectangle to reflect our new location
        dirty.add(_bounds);
        // and invalidate the whole shebang
        invalidate(dirty);
    }

    /**
     * Sprites have an orientation in one of the eight cardinal
     * directions: <code>DIR_NORTH</code>, <code>DIR_NORTHEAST</code>,
     * etc. Sprite derived classes can choose to override this member
     * function and select a different set of images based on their
     * orientation, or they can ignore the orientation information.
     */
    public void setOrientation (int orient)
    {
        _orient = orient;
    }

    /**
     * Returns the sprite's orientation as one of the eight cardinal
     * directions: <code>DIR_NORTH</code>, <code>DIR_NORTHEAST</code>,
     * etc.
     */
    public int getOrientation ()
    {
	return _orient;
    }

    /**
     * Initialize the sprite object with its variegated parameters.
     */
    protected void init (int x, int y, MultiFrameImage frames)
    {
        _x = x;
        _y = y;

        updateRenderOrigin();

	// initialize frame animation member data
        _frameIdx = 0;
        _animMode = NO_ANIMATION;
        _frameDelay = 1000L/DEFAULT_FRAME_RATE;

        setFrames(frames);
        invalidate();
    }        

    /**
     * Called by the sprite manager when a sprite is added to said manager
     * for management.
     *
     * @param mgr the sprite manager.
     */
    protected void setSpriteManager (SpriteManager mgr)
    {
        _spritemgr = mgr;
    }

    /**
     * Paint the sprite to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        gfx.drawImage(_frame, _bounds.x, _bounds.y, null);
    }

    /**
     * Paint the sprite's path, if any, to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paintPath (Graphics2D gfx)
    {
	if (_path != null) {
            _path.paint(gfx);
        }
    }

    /**
     * Returns whether the sprite is inside the given shape in
     * pixel coordinates.
     */
    public boolean inside (Shape shape)
    {
        return shape.contains(_x, _y);
    }

    /**
     * Returns whether the sprite's drawn rectangle intersects the given
     * shape in pixel coordinates.
     */
    public boolean intersects (Shape shape)
    {
        return shape.intersects(_bounds);
    }

    /**
     * Sets the animation mode for this sprite. The available modes are:
     *
     * <ul>
     * <li><code>TIME_BASED</code>: cues the animation based on a target
     * frame rate (specified via {@link #setFrameRate}).
     * <li><code>MOVEMENT_CUED</code>: ticks the animation to the next
     * frame every time the sprite is moved along its path.
     * <li><code>NO_ANIMATION</code>: disables animation.
     * </ul>
     *
     * @param mode the desired animation mode.
     */
    public void setAnimationMode (int mode)
    {
        _animMode = mode;
    }

    /**
     * Sets the number of frames per second desired for the sprite
     * animation. This is only used when the animation mode is
     * <code>TIME_BASED</code>.
     *
     * @param fps the desired frames per second.
     */
    public void setFrameRate (int fps)
    {
        _frameDelay = 1000L/fps;
    }

    /**
     * Set the image array used to render the sprite.
     *
     * @param frames the sprite images.
     */
    public void setFrames (MultiFrameImage frames)
    {
        if (frames == null) {
            Log.warning("Someone set up us the null frames! " +
                        "[sprite=" + this + "].");
            return;
        }

        _frames = frames;
        _frameIdx %= _frames.getFrameCount();
        _frame = _frames.getFrame(_frameIdx);

        // determine our drawing offsets and rendered rectangle size
        if (_frame == null) {
            _bounds.width = 0;
            _bounds.height = 0;

        } else {
            _bounds.width = _frame.getWidth(null);
            _bounds.height = _frame.getHeight(null);
        }

        updateRenderOffset();
        updateRenderOrigin();
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
        // save our path
        _path = path;

        // and initialize it
        _path.init(this, System.currentTimeMillis());
    }

    /**
     * Cancels any path that the sprite may currently be moving along.
     */
    public void cancelMove ()
    {
	// TODO: make sure we come to a stop on a full coordinate,
	// even in the case where we aborted a path mid-traversal.

        _path = null;
    }

    /**
     * Called by the active path when it begins.
     */
    protected void pathBeginning ()
    {
        // nothing for now
    }

    /**
     * Called by the active path when it has completed.
     */
    protected void pathCompleted ()
    {
        // keep a reference to the path just completed
        Path oldpath = _path;
        // clear out the path we've now finished
	_path = null;
        // inform observers that we've finished our path
        notifyObservers(new PathCompletedEvent(this, oldpath));

    }

    /**
     * Invalidate the sprite's bounding rectangle for later repainting.
     */
    public void invalidate ()
    {
        invalidate(null);
    }

    /**
     * Invalidate the given display rectangle for later repainting.
     * Passing <code>null</code> will simply invalidate the sprite's
     * entire rendered bounds.  Note that the given rectangle may be
     * destructively modified at some later time, e.g., by {@link
     * SpriteManager#getDirtyRects}.
     */
    protected void invalidate (Rectangle r)
    {
        if (_frame == null) {
            return;
        }

        if (_spritemgr != null) {
            _spritemgr.addDirtyRect((r != null) ? r : new Rectangle(_bounds));

        } else {
            Log.warning("Was invalidated but have no sprite manager " +
                        this + ".");
        }
    }

    /**
     * This method is called periodically by the sprite manager to give
     * the sprite a chance to update its state. The sprite manager will
     * attempt to call this with the desired refresh rate, but will drop
     * calls to tick if it can't keep up. Thus, a sprite should rely on
     * the timestamp information to compute elapsed progress if it wishes
     * to handle heavy loads gracefully.
     */
    public void tick (long timestamp)
    {
        int fcount = _frames.getFrameCount();
        boolean moved = false;

        // move the sprite along toward its destination, if any 
        if (_path != null) {
            moved = _path.updatePosition(this, timestamp);
        }

        // increment the display image if performing image animation
        int nfidx = _frameIdx;
        switch (_animMode) {
        case NO_ANIMATION:
            // nothing doing
            break;

        case TIME_BASED:
            nfidx = (int)((timestamp/_frameDelay) % fcount);
            break;

        case MOVEMENT_CUED:
            // update the frame if the sprite moved
            if (moved) {
                nfidx = (_frameIdx + 1) % fcount;
            }
            break;
        }

        // only update the sprite if our frame index changed
        if (nfidx != _frameIdx) {
            _frameIdx = nfidx;
            _frame = _frames.getFrame(_frameIdx);
            // dirty our rectangle since we've altered our display image
            invalidate();
        }
    }

    /**
     * Updates the sprite's render offset which is used to determine
     * where to place the top-left corner of the render bounds.
     */
    protected void updateRenderOffset ()
    {
        _rxoff = 0;
        _ryoff = 0;
    }

    /**
     * Update the coordinates at which the sprite image is drawn to
     * reflect the sprite's current position.
     */
    protected void updateRenderOrigin ()
    {
        // our render origin may differ from our location
        _bounds.x = _x + _rxoff;
        _bounds.y = _y + _ryoff;
    }

    /**
     * Add a sprite observer to observe this sprite's events.
     *
     * @param obs the sprite observer.
     */
    public void addSpriteObserver (SpriteObserver obs)
    {
	// create the observer list if it doesn't yet exist
	if (_observers == null) {
	    _observers = new ArrayList();
	}

	// make sure each observer observes only once
	if (_observers.contains(obs)) {
	    Log.info("Attempt to observe sprite already observing " +
		     "[sprite=" + this + ", obs=" + obs + "].");
	    return;
	}

	// add the observer
	_observers.add(obs);
    }

    /**
     * Inform all sprite observers of a sprite event.
     *
     * @param event the sprite event.
     */
    protected void notifyObservers (SpriteEvent event)
    {
	if (_observers != null) {
            // we pass this notification off to the sprite manager so that
            // it can dispatch all of the notifications at once after all
            // ticking has been completed
            _spritemgr.notifySpriteObservers(_observers, event);
        }
    }

    /**
     * Return a string representation of the sprite.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
	toString(buf);
        return buf.append("]").toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific sprite information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("x=").append(_x);
        buf.append(", y=").append(_y);
        buf.append(", fidx=").append(_frameIdx);
    }

    /** The images used to render the sprite. */
    protected MultiFrameImage _frames;

    /** The current frame being rendered. */
    protected Image _frame;

    /** The current frame index to render. */
    protected int _frameIdx;

    /** The orientation of this sprite. */
    protected int _orient = DIR_NONE;

    /** The location of the sprite in pixel coordinates. */
    protected int _x, _y;

    /** The offsets from our location to our rendered origin. */
    protected int _rxoff, _ryoff;

    /** Our rendered bounds in pixel coordinates. */
    protected Rectangle _bounds = new Rectangle();

    /** What type of animation is desired for this sprite. */
    protected int _animMode;

    /** For how many milliseconds to display an animation frame. */
    protected long _frameDelay;

    /** When moving, the path the sprite is traversing. */
    protected Path _path;

    /** The sprite observers observing this sprite. */
    protected ArrayList _observers;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
