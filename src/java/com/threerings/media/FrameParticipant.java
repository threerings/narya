//
// $Id: FrameParticipant.java,v 1.2 2002/06/18 22:25:33 mdb Exp $

package com.threerings.media;

import java.awt.Component;

/**
 * Provides a mechanism for participating in the frame tick managed by the
 * {@link FrameManager}.
 */
public interface FrameParticipant
{
    /**
     * This is called on all registered frame participants, one for every
     * frame. Following the tick the interface will be rendered, so
     * participants can prepare themselves for their upcoming render in
     * this method (making use of the timestamp provided for the frame if
     * choreography is desired between different participants).
     */
    public void tick (long tickStamp);

    /**
     * If a frame participant wishes also to be actively rendered every
     * frame rather than use passive rendering (which for Swing, at least,
     * is hijacked when using the frame manager such that we take care of
     * repainting dirty Swing components every frame into our off-screen
     * buffer), it can return a component here which will have {@link
     * Component#paint} called on it once per frame with a translated but
     * unclipped graphics object.
     *
     * <p> Because clipping is expensive in terms of rectangle object
     * allocation, frame participants are given the opportunity to do
     * their own clipping because they are likely to want to clip to a
     * more fine grained region than their entire bounds. If a particpant
     * does not wish to be actively rendered, it can safely return null.
     */
    public Component getComponent ();
}
