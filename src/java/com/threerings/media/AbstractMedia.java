//
// $Id: AbstractMedia.java,v 1.1 2002/10/08 21:03:37 ray Exp $

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;

/**
 * Something that can be rendered on the media panel.
 */
public abstract class AbstractMedia
{
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
     * @param tickStamp the system time for this tick.
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

    /**
     * Sets the render order associated with this media. Media
     * can be rendered in two layers; those with negative render order and
     * those with positive render order. In the same layer, they
     * will be rendered according to their render order's cardinal value
     * (least to greatest). Those with the same render order value will be
     * rendered in arbitrary order.
     *
     * <p> This must be set <em>before</em> the media is added to the
     * appropriate manager and must not change while the manager is
     * managing it. If you wish to change the render order, remove the
     * media from the manager, change the order and add it back again.
     */
    public void setRenderOrder (int renderOrder)
    {
        _renderOrder = renderOrder;
    }

    /**
     * Returns the render order of this media element.
     */
    public int getRenderOrder ()
    {
        return _renderOrder;
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
     * Called by the media manager after the media is removed from service.
     * Derived classes may override this method, but should be sure to
     * call <code>super.shutdown()</code>.
     */
    protected void shutdown ()
    {
        _mgr = null;
    }

    /**
     * Add the specified observer to this media element.
     */
    protected void addObserver (Object obs)
    {
        if (_observers == null) {
            _observers = new ArrayList();

        } else if (_observers.contains(obs)) {
            Log.info("Attempt to observe media already observing " + 
                "[media=" + this + ", obs=" + obs + "].");
            return;
        }

        _observers.add(obs);
    }

    /**
     * Dumps this media to a String object.
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
     * specific information to the string buffer.
     */
    protected void toString (StringBuffer buf)
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
    protected ArrayList _observers = null;
}
