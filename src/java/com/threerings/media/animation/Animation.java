//
// $Id: Animation.java,v 1.6 2002/04/25 16:23:30 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;

/**
 * The animation class is an abstract class that should be extended to
 * provide animation functionality. It is generally used in conjunction
 * with an {@link AnimationManager}.
 */
public abstract class Animation
{
    /**
     * Constructs an animation.
     *
     * @param bounds the animation rendering bounds.
     */
    public Animation (Rectangle bounds)
    {
        _bounds = bounds;
    }

    /**
     * Returns the render order of this animation.
     */
    public int getRenderOrder ()
    {
        return _renderOrder;
    }

    /**
     * Sets the render order associated with this animation.  Animations
     * can be rendered in two layers; those with negative render order and
     * those with positive render order.  Someday animations will be
     * rendered in each layer according to render order.
     */
    public void setRenderOrder (int value)
    {
        _renderOrder = value;
    }

    /**
     * Returns a rectangle containing all pixels rendered by this
     * animation.
     */
    public Rectangle getBounds ()
    {
        return _bounds;
    }

    /**
     * Called periodically by the {@link AnimationManager} to give the
     * animation a chance to do its thing.
     *
     * @param tickStamp the system time for this tick.
     */
    public abstract void tick (long tickStamp);

    /**
     * Called by the {@link AnimationManager} to request that the
     * animation render itself with the given graphics context. The
     * animation may wish to inspect the clipping region that has been set
     * on the graphics context to render itself more efficiently. This
     * method will only be called after it has been established that this
     * animation's bounds intersect the clipping region.
     */
    public abstract void paint (Graphics2D gfx);

    /**
     * This is called if the animation manager is paused for some length
     * of time and then unpaused. Animations should adjust any time stamps
     * they are maintaining internally by the delta so that time maintains
     * the illusion of flowing smoothly forward.
     */
    public void fastForward (long timeDelta)
    {
    }

    /**
     * Returns true if the animation has finished all of its business,
     * false if not.
     */
    public boolean isFinished ()
    {
        return _finished;
    }

    /**
     * Invalidates the bounds of this animation.
     */
    public void invalidate ()
    {
        _animmgr.getRegionManager().invalidateRegion(_bounds);
    }

    /**
     * Called when the animation is finished and the animation manager has
     * removed it from service.
     */
    protected void didFinish ()
    {
        // nothing for now
    }

    /**
     * Adds an animation observer to this animation's list of observers.
     */
    public void addAnimationObserver (AnimationObserver obs)
    {
	// create the observer list if it doesn't yet exist
	if (_observers == null) {
	    _observers = new ArrayList();
	}

	// make sure each observer observes only once
	if (_observers.contains(obs)) {
	    Log.info("Attempt to observe animation already observing " +
		     "[anim=" + this + ", obs=" + obs + "].");
	    return;
	}

	// add the observer
	_observers.add(obs);
    }

    /**
     * Notifies any animation observers that the given animation event has
     * occurred.
     */
    public void notifyObservers (AnimationEvent e)
    {
        int size = (_observers == null) ? 0 : _observers.size();
        for (int ii = 0; ii < size; ii++) {
            ((AnimationObserver)_observers.get(ii)).handleEvent(e);
        }
    }

    /**
     * Return a string representation of the animation.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
	toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Called automatically when an animation is added to an animation
     * manager for management.
     */
    protected void setAnimationManager (AnimationManager animmgr)
    {
        _animmgr = animmgr;
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific animation information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("bounds=").append(StringUtil.toString(_bounds));
    }

    /** Our animation manager. */
    protected AnimationManager _animmgr;

    /** Whether the animation is finished. */
    protected boolean _finished = false;

    /** The animation bounds. */
    protected Rectangle _bounds;

    /** The render order of this animation. */
    protected int _renderOrder;

    /** The list of animation observers. */
    protected ArrayList _observers;
}
