//
// $Id: AnimationObserver.java,v 1.3 2004/08/18 01:33:32 mdb Exp $

package com.threerings.media.animation;

/**
 * An interface to be implemented by classes that would like to observe an
 * {@link Animation} and be notified of interesting events relating to it.
 */
public interface AnimationObserver
{
    /**
     * Called the first time this animation is ticked.
     */
    public void animationStarted (Animation anim, long when);

    /**
     * Called when the observed animation has completed.
     */
    public void animationCompleted (Animation anim, long when);
}
