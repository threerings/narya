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

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

/**
 * Something that can be rendered on the media panel.
 */
public abstract class AbstractMedia
    implements Shape
{
    /** A {@link #_renderOrder} value at or above which, indicates that this
     * media is in the HUD (heads up display) and should not scroll when the
     * view scrolls. */
    public static final int HUD_LAYER = 65536;

    /**
     * Instantiate an abstract media object.
     */
    public AbstractMedia (Rectangle bounds)
    {
        _bounds = bounds;
    }

    /**
     * Called periodically by this media's manager to give it
     * a chance to do its thing.
     *
     * @param tickStamp a time stamp associated with this tick.
     * <em>Note:</em> this is not obtained from a call to {@link
     * System#currentTimeMillis} and cannot be compared to timestamps
     * obtained there from.
     */
    public abstract void tick (long tickStamp);

    /**
     * Called by the appropriate manager to request that the
     * media render itself with the given graphics context. The
     * media may wish to inspect the clipping region that has been set
     * on the graphics context to render itself more efficiently. This
     * method will only be called after it has been established that this
     * media's bounds intersect the clipping region.
     */
    public abstract void paint (Graphics2D gfx);

    /**
     * Called when the appropriate media manager has been paused for some
     * length of time and is then unpaused. Media should adjust any time stamps
     * that are maintained internally forward by the delta so that time
     * maintains the illusion of flowing smoothly forward.
     */
    public void fastForward (long timeDelta)
    {
        // adjust our first tick stamp
        _firstTick += timeDelta;
    }

    /**
     * Invalidate the media's bounding rectangle for later painting.
     */
    public void invalidate ()
    {
        if (_mgr != null) {
            _mgr.getRegionManager().invalidateRegion(_bounds);
        }
    }

    /**
     * Set the location.
     */
    public void setLocation (int x, int y)
    {
        _bounds.x = x;
        _bounds.y = y;
    }

    /**
     * Returns a rectangle containing all the pixels rendered by this media.
     */
    public Rectangle getBounds ()
    {
        return _bounds;
    }

    // documentation inherited from interface Shape
    public Rectangle2D getBounds2D ()
    {
        return _bounds;
    }

    // documentation inherited from interface Shape
    public boolean contains (double x, double y)
    {
        return _bounds.contains(x, y);
    }

    // documentation inherited from interface Shape
    public boolean contains (Point2D p)
    {
        return _bounds.contains(p);
    }

    // documentation inherited from interface Shape
    public boolean intersects (double x, double y, double w, double h)
    {
        return _bounds.intersects(x, y, w, h);
    }

    // documentation inherited from interface Shape
    public boolean intersects (Rectangle2D r)
    {
        return _bounds.intersects(r);
    }

    // documentation inherited from interface Shape
    public boolean contains (double x, double y, double w, double h)
    {
        return _bounds.contains(x, y, w, h);
    }

    // documentation inherited from interface Shape
    public boolean contains (Rectangle2D r)
    {
        return _bounds.contains(r);
    }

    // documentation inherited from interface Shape
    public PathIterator getPathIterator (AffineTransform at)
    {
        return _bounds.getPathIterator(at);
    }

    // documentation inherited from interface Shape
    public PathIterator getPathIterator (AffineTransform at, double flatness)
    {
        return _bounds.getPathIterator(at, flatness);
    }

    /**
     * Sets the render order associated with this media. Media
     * can be rendered in two layers; those with negative render order and
     * those with positive render order. In the same layer, they
     * will be rendered according to their render order's cardinal value
     * (least to greatest). Those with the same render order value will be
     * rendered in arbitrary order.
     *
     * <p>This method may not be called during a tick.
     *
     * @see #HUD_LAYER
     */
    public void setRenderOrder (int renderOrder)
    {
        if (_renderOrder != renderOrder) {
            _renderOrder = renderOrder;
            if (_mgr != null) {
                _mgr.renderOrderDidChange(this);
                invalidate();
            }
        }
    }

    /**
     * Returns the render order of this media element.
     */
    public int getRenderOrder ()
    {
        return _renderOrder;
    }

    /**
     * Queues the supplied notification up to be dispatched to this
     * abstract media's observers.
     */
    public void queueNotification (ObserverList.ObserverOp amop)
    {
        if (_observers != null) {
            if (_mgr != null) {
                _mgr.queueNotification(_observers, amop);
            } else {
                Log.warning("Have no manager, dropping notification " +
                            "[media=" + this + ", op=" + amop + "].");
            }
        }
    }

    /**
     * Called by the {@link AbstractMediaManager} when we are in a
     * {@link VirtualMediaPanel} that just scrolled.
     */
    public void viewLocationDidChange (int dx, int dy)
    {
        if (_renderOrder >= HUD_LAYER) {
            setLocation(_bounds.x + dx, _bounds.y + dy);
        }
    }

    /**
     * Dumps this media to a String object.
     */
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(StringUtil.shortClassName(this));
        buf.append("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Initialize the media.
     */
    protected final void init (AbstractMediaManager manager)
    {
        _mgr = manager;
        init();
    }

    /**
     * Called when the media has had its manager set.
     * Derived classes may override this method, but should be sure to
     * call <code>super.init()</code>.
     */
    protected void init ()
    {
    }

    /**
     * Prior to the first call to {@link #tick} on an abstract media, this
     * method is called by the {@link AbstractMediaManager}. It is called
     * during the normal tick cycle, immediately prior to the first call
     * to {@link #tick}.
     *
     * <p><em>Note:</em> It is imperative that
     * <code>super.willStart()</code> is called by any entity that
     * overrides this method because the {@link AbstractMediaManager}
     * depends on the setting of the {@link #_firstTick} value to know
     * whether or not to call this method.
     */
    protected void willStart (long tickStamp)
    {
        _firstTick = tickStamp;
    }

    /**
     * Called by the media manager after the media is removed from service.
     * Derived classes may override this method, but should be sure to
     * call <code>super.shutdown()</code>.
     */
    protected void shutdown ()
    {
        invalidate();
        _mgr = null;
    }

    /**
     * Add the specified observer to this media element.
     */
    protected void addObserver (Object obs)
    {
        if (_observers == null) {
            _observers = new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);
        }
        _observers.add(obs);
    }

    /**
     * Remove the specified observer from this media element.
     */
    protected void removeObserver (Object obs)
    {
        if (_observers != null) {
            _observers.remove(obs);
        }
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific information to the string buffer.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("bounds=").append(StringUtil.toString(_bounds));
        buf.append(", renderOrder=").append(_renderOrder);
    }

    /** The layer in which to render. */
    protected int _renderOrder = 0;

    /** The bounds of the media's rendering area.  */
    protected Rectangle _bounds;

    /** Our manager. */
    protected AbstractMediaManager _mgr;

    /** Our observers. */
    protected ObserverList _observers = null;

    /** The tick stamp associated with our first call to {@link #tick}.
     * This is set up automatically in {@link #willStart}. */
    protected long _firstTick;
}
