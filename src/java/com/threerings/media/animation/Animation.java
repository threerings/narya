//
// $Id: Animation.java,v 1.9 2002/10/08 21:03:37 ray Exp $

package com.threerings.media.animation;

import java.awt.Rectangle;

import com.threerings.media.AbstractMedia;
import com.threerings.media.Log;

/**
 * The animation class is an abstract class that should be extended to
 * provide animation functionality. It is generally used in conjunction
 * with an {@link AnimationManager}.
 */
public abstract class Animation extends AbstractMedia
{
    /**
     * Constructs an animation.
     *
     * @param bounds the animation rendering bounds.
     */
    public Animation (Rectangle bounds)
    {
        super(bounds);
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
     * If this animation has run to completion, it can be reset to prepare
     * it for another go.
     */
    public void reset ()
    {
        _finished = false;
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
        addObserver(obs);
    }

    /**
     * Notifies any animation observers that the given animation event has
     * occurred.
     */
    public void notifyObservers (AnimationEvent event)
    {
        if (_observers != null) {
            _mgr.queueNotification(_observers, event);
        }
    }

    /** Whether the animation is finished. */
    protected boolean _finished = false;
}
