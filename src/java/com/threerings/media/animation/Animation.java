//
// $Id: Animation.java,v 1.12 2004/08/18 01:33:32 mdb Exp $

package com.threerings.media.animation;

import java.awt.Rectangle;

import com.samskivert.util.ObserverList;

import com.threerings.media.AbstractMedia;

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

    // documentation inherited
    protected void willStart (long tickStamp)
    {
        super.willStart(tickStamp);
        queueNotification(new AnimStartedOp(this, tickStamp));
    }

    /**
     * Called when the animation is finished and the animation manager is
     * about to remove it from service.
     */
    protected void willFinish (long tickStamp)
    {
        queueNotification(new AnimCompletedOp(this, tickStamp));
    }

    /**
     * Called when the animation is finished and the animation manager has
     * removed it from service.
     */
    protected void didFinish (long tickStamp)
    {
    }

    /**
     * Adds an animation observer to this animation's list of observers.
     */
    public void addAnimationObserver (AnimationObserver obs)
    {
        addObserver(obs);
    }

    /** Whether the animation is finished. */
    protected boolean _finished = false;

    /** Used to dispatch {@link AnimationObserver#animationStarted}. */
    protected static class AnimStartedOp implements ObserverList.ObserverOp
    {
        public AnimStartedOp (Animation anim, long when) {
            _anim = anim;
            _when = when;
        }

        public boolean apply (Object observer) {
            ((AnimationObserver)observer).animationStarted(_anim, _when);
            return true;
        }

        protected Animation _anim;
        protected long _when;
    }

    /** Used to dispatch {@link AnimationObserver#animationCompleted}. */
    protected static class AnimCompletedOp implements ObserverList.ObserverOp
    {
        public AnimCompletedOp (Animation anim, long when) {
            _anim = anim;
            _when = when;
        }

        public boolean apply (Object observer) {
            ((AnimationObserver)observer).animationCompleted(_anim, _when);
            return true;
        }

        protected Animation _anim;
        protected long _when;
    }
}
