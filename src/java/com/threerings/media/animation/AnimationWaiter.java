//
// $Id: AnimationWaiter.java,v 1.3 2003/04/30 00:45:02 mdb Exp $

package com.threerings.media.animation;

/**
 * An abstract class that simplifies a common animation usage case in
 * which a number of animations are created and the creator would like to
 * be able to perform specific actions when each animation has completed
 * (see {@link #animationDidFinish} and/or when all animations are
 * finished (see {@link #allAnimationsFinished}.)
 */
public abstract class AnimationWaiter
    implements AnimationObserver
{
    /**
     * Adds an animation to the animation waiter for observation.
     */
    public void addAnimation (Animation anim)
    {
        anim.addAnimationObserver(this);
        _animCount++;
    }

    /**
     * Adds the supplied animations to the animation waiter for
     * observation.
     */
    public void addAnimations (Animation[] anims)
    {
        int acount = anims.length;
        for (int ii = 0; ii < acount; ii++) {
            addAnimation(anims[ii]);
        }
    }

    // documentation inherited from interface
    public void animationCompleted (Animation anim, long when)
    {
        // note that the animation is finished
        animationDidFinish(anim);
        _animCount--;

        // let derived classes know when all is done
        if (_animCount == 0) {
            allAnimationsFinished();
        }
    }

    /**
     * Called when an animation being observed by the waiter has
     * completed its business.  Derived classes may wish to override
     * this method to engage in their unique antics.
     */
    protected void animationDidFinish (Animation anim)
    {
        // nothing for now
    }

    /**
     * Called when all animations being observed by the waiter have
     * completed their business.  Derived classes may wish to override
     * this method to engage in their unique antics.
     */
    protected void allAnimationsFinished ()
    {
        // nothing for now
    }

    /** The number of animations. */
    protected int _animCount;
}
