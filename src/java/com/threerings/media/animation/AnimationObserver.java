//
// $Id: AnimationObserver.java,v 1.2 2003/04/30 00:45:02 mdb Exp $

package com.threerings.media.animation;

/**
 * An interface to be implemented by classes that would like to observe an
 * {@link Animation} and be notified of interesting events relating to it.
 */
public interface AnimationObserver
{
    /**
     * Called when the observed animation has completed.
     */
    public void animationCompleted (Animation anim, long when);
}
