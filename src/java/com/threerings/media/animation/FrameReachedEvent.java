//
// $Id: FrameReachedEvent.java,v 1.1 2002/09/20 21:35:11 ray Exp $

package com.threerings.media.animation;

/**
 * Indicates that a particular frame was reached in an animation.
 */
public class FrameReachedEvent extends AnimationEvent
{
    /**
     * Construct a FrameReachedEvent.
     */
    public FrameReachedEvent (Animation anim, int frameIdx, int frameSeq)
    {
        super(anim);
        _frameIdx = frameIdx;
        _frameSeq = frameSeq;
    }

    /**
     * Return the index in the multi-frame animation that was reached.
     */
    public int getFrameIndex ()
    {
        return _frameIdx;
    }

    /**
     * Return the sequence number of the frame that was reached.
     * This may be different from the index, a MultiFunction FrameSequencer
     * can show a particular frame more than once.
     */
    public int getFrameSequence ()
    {
        return _frameSeq;
    }

    /** Frame index and sequence for the event. */
    protected int _frameIdx, _frameSeq;
}
