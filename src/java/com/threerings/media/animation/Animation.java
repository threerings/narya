//
// $Id: Animation.java,v 1.3 2002/03/14 21:09:01 shaper Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;

import com.threerings.media.Log;

/**
 * The animation class is an abstract class that should be extended to
 * provide animation functionality for use with the {@link
 * AnimationManager} and an {@link AnimatedView}.
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
     * Called by the {@link AnimationManager} to allow the animation to
     * render itself to the given graphics context.
     */
    public abstract void paint (Graphics2D gfx);

    /**
     * Called periodically by the {@link AnimationManager} to give the
     * animation a chance to do its thing.
     */
    public abstract void tick (long timestamp);

    /**
     * Invalidates the animation's bounds for later re-rendering by the
     * {@link AnimationManager}.
     */
    public void invalidate ()
    {
        _animmgr.addDirtyRect(new Rectangle(_bounds));
    }

    /**
     * Invalidates the specified rectangle for later re-rendering by the
     * {@link AnimationManager}.
     */
    public void invalidate (int x, int y, int width, int height)
    {
        _animmgr.addDirtyRect(new Rectangle(x, y, width, height));
    }

    /**
     * Returns whether the animation has finished all of its business.
     */
    public boolean isFinished ()
    {
        return _finished;
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
     * Notifies any animation observers that the given animation is
     * finished.
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
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific animation information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("bounds=").append(_bounds);
    }

    /**
     * Called by the animation manager when an animation is added to said
     * manager for management.
     *
     * @param animmgr the animation manager.
     * @param view the animated view.
     */
    protected void setAnimationManager (AnimationManager animmgr)
    {
        _animmgr = animmgr;
    }

    /** Whether the animation is finished. */
    protected boolean _finished = false;

    /** The animation bounds. */
    protected Rectangle _bounds;

    /** The list of animation observers. */
    protected ArrayList _observers;

    /** The animation manager. */
    protected AnimationManager _animmgr;
}
