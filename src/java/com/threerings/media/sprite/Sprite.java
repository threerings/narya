//
// $Id: Sprite.java,v 1.44 2002/05/29 23:27:14 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;

import com.threerings.util.DirectionCodes;

import com.threerings.media.Log;

/**
 * The sprite class represents a single moveable object in an animated
 * view. A sprite has a position and orientation within the view, and can
 * be moved along a path.
 */
public abstract class Sprite
    implements DirectionCodes
{
    /**
     * Constructs a sprite with a default initial location of <code>(0,
     * 0)</code>.
     */
    public Sprite ()
    {
        this(0, 0);
    }

    /**
     * Constructs a sprite initially positioned at the specified location.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     */
    public Sprite (int x, int y)
    {
        _x = x;
        _y = y;
    }

    /**
     * Returns true if this sprite should scroll along with the view or
     * false if it should remain fixed relative to the view when it
     * scrolls.
     */
    public boolean scrollsWithView ()
    {
        return _scrollsWithView;
    }

    /**
     * Specifies whether this sprite scrolls when the view scrolls or
     * remains fixed relative to the view.
     */
    public void setScrollsWithView (boolean scrollsWithView)
    {
        _scrollsWithView = scrollsWithView;
    }

    /**
     * Called by the sprite manager to initialize the sprite when a sprite
     * is added to said manager for management.
     *
     * @param spritemgr the sprite manager.
     */
    protected void init (SpriteManager spritemgr)
    {
        _spritemgr = spritemgr;

        updateRenderOrigin();
    }

    /**
     * Called by the sprite manager after the sprite is removed from
     * service.  Derived classes may override this method but should be
     * sure to call <code>super.shutdown()</code>.
     */
    protected void shutdown ()
    {
        _spritemgr = null;
    }

    /**
     * Returns the sprite's x position in screen coordinates. This is the
     * x coordinate of the sprite's origin, not the upper left of its
     * bounds.
     */
    public int getX ()
    {
        return _x;
    }

    /**
     * Returns the sprite's y position in screen coordinates. This is the
     * y coordinate of the sprite's origin, not the upper left of its
     * bounds.
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
     * Sprites have an orientation in one of the eight cardinal
     * directions: <code>NORTH</code>, <code>NORTHEAST</code>, etc.
     * Derived classes can choose to override this member function and
     * select a different set of images based on their orientation, or
     * they can ignore the orientation information.
     *
     * @see DirectionCodes
     */
    public void setOrientation (int orient)
    {
        _orient = orient;
    }

    /**
     * Returns the sprite's orientation as one of the eight cardinal
     * directions: <code>NORTH</code>, <code>NORTHEAST</code>, etc.
     *
     * @see DirectionCodes
     */
    public int getOrientation ()
    {
	return _orient;
    }

    /**
     * Returns the render order of this sprite.
     */
    public int getRenderOrder ()
    {
        return _renderOrder;
    }

    /**
     * Sets the render order associated with this animation.  Sprites can
     * be rendered in two layers; those with negative render order and
     * those with positive render order.  Someday sprites will be rendered
     * in each layer according to render order.
     */
    public void setRenderOrder (int value)
    {
        _renderOrder = value;
    }

    /**
     * Moves the sprite to the specified location.
     *
     * @param x the x-position in pixels.
     * @param y the y-position in pixels.
     */
    public void setLocation (int x, int y)
    {
        // dirty our current bounds
        invalidate();

        // move ourselves
        _x = x;
        _y = y;

        // we need to update our draw position which is based on the
        // size of our current bounds
        updateRenderOrigin();

        // dirty our new bounds
        invalidate();
    }

    /**
     * Paint the sprite to the specified graphics context.
     *
     * @param gfx the graphics context.
     */
    public void paint (Graphics2D gfx)
    {
        gfx.draw(_bounds);
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
     * Returns true if the sprite's bounds contain the specified point,
     * false if not.
     */
    public boolean contains (int x, int y)
    {
        return _bounds.contains(x, y);
    }

    /**
     * Returns true if the sprite's bounds contain the specified point,
     * false if not.
     */
    public boolean hitTest (int x, int y)
    {
        return _bounds.contains(x, y);
    }

    /**
     * Returns whether the sprite is inside the given shape in pixel
     * coordinates.
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
     * Returns true if this sprite is currently following a path, false if
     * it is not.
     */
    public boolean isMoving ()
    {
        return (_path != null);
    }

    /**
     * Set the sprite's active path and start moving it along its merry
     * way.  If the sprite is already moving along a previous path the old
     * path will be lost and the new path will begin to be traversed.
     *
     * @param path the path to follow.
     */
    public void move (Path path)
    {
        // initialize the path
        path.init(this, System.currentTimeMillis());

        // and save it off
        _path = path;
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
    public void pathBeginning ()
    {
        // nothing for now
    }

    /**
     * Called by the active path when it has completed.
     */
    public void pathCompleted ()
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
        if (_spritemgr != null) {
            _spritemgr.getRegionManager().invalidateRegion(_bounds);
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
    public void tick (long tickStamp)
    {
        // if we've a path, move the sprite along toward its destination
        if (_path != null) {
            _path.updatePosition(this, tickStamp);
        }
    }

    /**
     * This is called if the sprite manager is paused for some length of
     * time and then unpaused. Sprites should adjust any time stamps they
     * are maintaining internally by the delta so that time maintains the
     * illusion of flowing smoothly forward.
     */
    public void fastForward (long timeDelta)
    {
        // fast forward any path we're following
        if (_path != null) {
            _path.fastForward(timeDelta);
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
     * Called by the sprite manager to let us know that the view we occupy
     * is scrolling by the specified amount. A sprite anchored to the view
     * will simply update its coordinates; a fixed sprite will remain in
     * the same position but invalidate its bounds and its scrolled
     * bounds.
     */
    protected void viewWillScroll (int dx, int dy)
    {
        if (_scrollsWithView) {
            // update our coordinates
            _x -= dx;
            _y -= dy;
            _bounds.x -= dx;
            _bounds.y -= dy;

            // and let any path that we're following know what's up
            if (_path != null) {
                _path.viewWillScroll(dx, dy);
            }

        } else {
            // if we're not scrolling, we need to dirty our bounds and the
            // part that was just scrolled out from under us
            Rectangle dirty = new Rectangle(_bounds);

            // expand the rectangle to contain the scrolled regions
            int ex = dirty.x - dx, ey = dirty.y - dy;
            if (dx < 0) {
                ex += dirty.width;
            }
            if (dy < 0) {
                ey += dirty.height;
            }
            dirty.add(ex, ey);

            // give the dirty rectangle to the region manager
            _spritemgr.getRegionManager().addDirtyRegion(dirty);
        }
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
        StringBuffer buf = new StringBuffer("[");
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
    }

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The location of the sprite in pixel coordinates. */
    protected int _x, _y;

    /** The offsets from our location to our rendered origin. */
    protected int _rxoff, _ryoff;

    /** Our rendered bounds in pixel coordinates. */
    protected Rectangle _bounds = new Rectangle();

    /** The orientation of this sprite. */
    protected int _orient = NONE;

    /** True if we should move along with the view when it scrolls, false
     * if we should remain fixed relative to the viewport. We are fixed by
     * default. */
    protected boolean _scrollsWithView = false;

    /** When moving, the path the sprite is traversing. */
    protected Path _path;

    /** The render order of this sprite. */
    protected int _renderOrder;

    /** The sprite observers observing this sprite. */
    protected ArrayList _observers;
}
