//
// $Id: FrameSequencer.java,v 1.1 2002/09/17 20:07:28 mdb Exp $

package com.threerings.media.util;

import com.threerings.media.util.MultiFrameImage;

/**
 * Used to control animation timing when displaying a {@link
 * MultiFrameImage}. This interface allows for constant framerates, or
 * more sophisticated animation timing.
 */
public interface FrameSequencer
{
    /**
     * Called prior to the execution of an animation sequence (not
     * necessarily immediately, so time stamps should be obtained in the
     * first call to tick).
     *
     * @param source the multie-frame image that is providing the
     * animation frames.
     */
    public void init (MultiFrameImage source);

    /**
     * Called every display frame, the frame sequencer should return the
     * index of the animation frame that should be displayed during this
     * tick. If the sequencer returns -1, it is taken as an indication
     * that the animation is finished and should be stopped.
     */
    public int tick (long tickStamp);

    /**
     * Called if the display is paused for some length of time and then
     * unpaused. Sequencers should adjust any time stamps they are
     * maintaining internally by the delta so that time maintains the
     * illusion of flowing smoothly forward.
     */
    public void fastForward (long timeDelta);

    /**
     * A frame sequencer that delivers a constant frame rate in either one
     * shot or looping mode.
     */
    public static class ConstantRate implements FrameSequencer
    {
        /**
         * Creates a constant rate frame sequencer with the desired target
         * frames per second.
         *
         * @param framesPerSecond the target frames per second.
         * @param loop if false, the sequencer will report the end of
         * the animation after progressing through all of the frames once,
         * otherwise it will loop indefinitely.
         */
        public ConstantRate (double framesPerSecond, boolean loop)
        {
            _millisPerFrame = (long)(1000d / _millisPerFrame);
            _loop = loop;
        }

        // documentation inherited from interface
        public void init (MultiFrameImage source)
        {
            _frameCount = source.getFrameCount();
            _startStamp = 0l;
        }

        // documentation inherited from interface
        public int tick (long tickStamp)
        {
            // obtain our starting timestamp if we don't already have one
            if (_startStamp == 0) {
                _startStamp = tickStamp;
            }

            // compute our current frame index
            int frameIdx = (int)((tickStamp - _startStamp) / _millisPerFrame);

            // if we're not looping and we've exhausted our frames, we
            // return -1 to indicate that the animation should stop
            return (!_loop && frameIdx >= _frameCount) ? -1 : frameIdx;
        }

        // documentation inherited from interface
        public void fastForward (long timeDelta)
        {
            _startStamp += timeDelta;
        }

        protected long _millisPerFrame;
        protected boolean _loop;
        protected int _frameCount;
        protected long _startStamp;
    }
}
