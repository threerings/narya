//
// $Id: AnimationObserver.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

package com.threerings.media.animation;

/**
 * An interface to be implemented by classes that would like to observe an
 * {@link Animation} and be notified of interesting events relating to it.
 */
public interface AnimationObserver
{
    /**
     * This method is called by the {@link AnimationManager} when
     * something interesting involving the animation happens.
     *
     * @param event the animation event.
     */
    public void handleEvent (AnimationEvent event);
}
