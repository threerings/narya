//
// $Id: SequencedAnimationObserver.java,v 1.1 2003/04/30 00:45:02 mdb Exp $

package com.threerings.media.animation;

/**
 * Extends the animation observer interface with extra goodies.
 */
public interface SequencedAnimationObserver extends AnimationObserver
{
    /**
     * Called when the observed animation -- previously configured with an
     * {@link AnimationFrameSequencer} -- reached the specified frame.
     */
    public void frameReached (Animation anim, long when,
                              int frameIdx, int frameSeq);
}
