//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import com.samskivert.util.ObserverList;
import com.threerings.util.DirectionCodes;

import com.threerings.media.AbstractMedia;
import com.threerings.media.util.Path;
import com.threerings.media.util.Pathable;

/**
 * The sprite class represents a single moveable object in an animated
 * view. A sprite has a position and orientation within the view, and can
 * be moved along a path.
 */
public abstract class Sprite extends AbstractMedia
    implements DirectionCodes, Pathable
{
    /**
     * Constructs a sprite with an initially invalid location. Because
     * sprite derived classes generally want to get in on the business
     * when a sprite's location is set, it is not safe to do so in the
     * constructor because their derived methods will be called before
     * their constructor has been called. Thus a sprite should be fully
     * constructed and <em>then</em> its location should be set.
     */
    public Sprite ()
    {
        this(0, 0);
    }

    /**
     * Constructs a sprite with the supplied dimensions. Because
     * sprite derived classes generally want to get in on the business
     * when a sprite's location is set, it is not safe to do so in the
     * constructor because their derived methods will be called before
     * their constructor has been called. Thus a sprite should be fully
     * constructed and <em>then</em> its location should be set.
     */
    public Sprite (int width, int height)
    {
        super(new Rectangle(0, 0, width, height));
    }

    /**
     * Returns the sprite's x position in screen coordinates. This is the
     * x coordinate of the sprite's origin, not the upper left of its
     * bounds.
     */
    public int getX ()
    {
        return _ox;
    }

    /**
     * Returns the sprite's y position in screen coordinates. This is the
     * y coordinate of the sprite's origin, not the upper left of its
     * bounds.
     */
    public int getY ()
    {
        return _oy;
    }

    /**
     * Returns the offset to the sprite's origin from the upper-left of
     * the sprite's image.
     */
    public int getXOffset ()
    {
        return _oxoff;
    }

    /**
     * Returns the offset to the sprite's origin from the upper-left of
     * the sprite's image.
     */
    public int getYOffset ()
    {
        return _oyoff;
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

    // documentation inherited
    public void setLocation (int x, int y)
    {
        // start with our current bounds
        Rectangle dirty = new Rectangle(_bounds);

        // move ourselves
        _ox = x;
        _oy = y;

        // we need to update our draw position which is based on the size
        // of our current bounds
        updateRenderOrigin();

        // grow the dirty rectangle to incorporate our new bounds and pass
        // the dirty region to our region manager
        if (_mgr != null) {
            // if our new bounds intersect our old bounds, grow a single
            // dirty rectangle to incorporate them both
            if (_bounds.intersects(dirty)) {
                dirty.add(_bounds);
            } else {
                // otherwise invalidate our new bounds separately
                _mgr.getRegionManager().invalidateRegion(_bounds);
            }
            _mgr.getRegionManager().addDirtyRegion(dirty);
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.drawRect(_bounds.x, _bounds.y, _bounds.width-1, _bounds.height-1);
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
        return shape.contains(_ox, _oy);
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
        // if there's a previous path, let it know that it's going away
        cancelMove();

        // save off this path
        _path = path;

        // we'll initialize it on our next tick thanks to a zero path stamp
        _pathStamp = 0;
    }

    /**
     * Cancels any path that the sprite may currently be moving along.
     */
    public void cancelMove ()
    {
        if (_path != null) {
            Path oldpath = _path;
            _path = null;
            oldpath.wasRemoved(this);
            if (_observers != null) {
                _observers.apply(new CancelledOp(this, oldpath));
            }
        }
    }

    /**
     * Returns the path being followed by this sprite or null if the
     * sprite is not following a path.
     */
    public Path getPath ()
    {
        return _path;
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
    public void pathCompleted (long timestamp)
    {
        Path oldpath = _path;
        _path = null;
        oldpath.wasRemoved(this);
        if (_observers != null) {
            _observers.apply(new CompletedOp(this, oldpath, timestamp));
        }
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        tickPath(tickStamp);
    }

    /**
     * Ticks any path assigned to this sprite.
     *
     * @return true if the path relocated the sprite as a result of this
     * tick, false if it remained in the same position.
     */
    protected boolean tickPath (long tickStamp)
    {
        if (_path == null) {
            return false;
        }

        // initialize the path if we haven't yet
        if (_pathStamp == 0) {
            _path.init(this, _pathStamp = tickStamp);
        }

        // it's possible that as a result of init() the path completed and
        // removed itself with a call to pathCompleted(), so we have to be
        // careful here
        return (_path == null) ? true : _path.tick(this, tickStamp);
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        // fast forward any path we're following
        if (_path != null) {
            _path.fastForward(timeDelta);
        }
    }

    /**
     * Update the coordinates at which the sprite image is drawn to
     * reflect the sprite's current position.
     */
    protected void updateRenderOrigin ()
    {
        // our bounds origin may differ from the sprite's origin
        _bounds.x = _ox - _oxoff;
        _bounds.y = _oy - _oyoff;
    }

    /**
     * Add a sprite observer to observe this sprite's events.
     *
     * @param obs the sprite observer.
     */
    public void addSpriteObserver (Object obs)
    {
        addObserver(obs);
    }

    /**
     * Remove a sprite observer.
     */
    public void removeSpriteObserver (Object obs)
    {
        removeObserver(obs);
    }

    // documentation inherited
    public void viewLocationDidChange (int dx, int dy)
    {
        if (_renderOrder >= HUD_LAYER) {
            setLocation(_ox + dx, _oy + dy);
        }
    }

    // documentation inherited
    public void shutdown ()
    {
        super.shutdown();
        cancelMove(); // cancel any active path
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", ox=").append(_ox);
        buf.append(", oy=").append(_oy);
        buf.append(", oxoff=").append(_oxoff);
        buf.append(", oyoff=").append(_oyoff);
    }

    /** Used to dispatch {@link PathObserver#pathCancelled}. */
    protected static class CancelledOp implements ObserverList.ObserverOp
    {
        public CancelledOp (Sprite sprite, Path path) {
            _sprite = sprite;
            _path = path;
        }

        public boolean apply (Object observer) {
            if (observer instanceof PathObserver) {
                ((PathObserver)observer).pathCancelled(_sprite, _path);
            }
            return true;
        }

        protected Sprite _sprite;
        protected Path _path;
    }

    /** Used to dispatch {@link PathObserver#pathCompleted}. */
    protected static class CompletedOp implements ObserverList.ObserverOp
    {
        public CompletedOp (Sprite sprite, Path path, long when) {
            _sprite = sprite;
            _path = path;
            _when = when;
        }

        public boolean apply (Object observer) {
            if (observer instanceof PathObserver) {
                ((PathObserver)observer).pathCompleted(_sprite, _path, _when);
            }
            return true;
        }

        protected Sprite _sprite;
        protected Path _path;
        protected long _when;
    }

    /** The location of the sprite's origin in pixel coordinates. If the
     * sprite positions itself via a hotspot that is not the upper left
     * coordinate of the sprite's bounds, the offset to the hotspot should
     * be maintained in {@link #_oxoff} and {@link #_oyoff}. */
    protected int _ox = Integer.MIN_VALUE, _oy = Integer.MIN_VALUE;

    /** The offsets from our upper left coordinate to our origin (or hot
     * spot). Derived classes will need to update these values if the
     * sprite's origin is not coincident with the upper left coordinate of
     * its bounds.  */
    protected int _oxoff, _oyoff;

    /** The orientation of this sprite. */
    protected int _orient = NONE;

    /** When moving, the path the sprite is traversing. */
    protected Path _path;

    /** The timestamp at which we started along our path. */
    protected long _pathStamp;
}
