//
// $Id: AnimationFrameSequencer.java,v 1.3 2002/11/05 20:51:50 mdb Exp $

package com.threerings.media.animation;

import java.util.Arrays;

import com.threerings.media.util.FrameSequencer;
import com.threerings.media.util.MultiFrameImage;

/**
 * Used to control animation timing when displaying an animation.
 */
public interface AnimationFrameSequencer extends FrameSequencer
{
    /**
     * Called after init to set the animation.
     */
    public void setAnimation (Animation anim);

    /**
     * A frame sequencer that can step through a series of frames in any order
     * and speed and send events when specific frames are reached.
     */
    public static class MultiFunction implements AnimationFrameSequencer
    {
        /**
         * Creates the simplest multifunction frame sequencer.
         *
         * @param sequence the ordering to display the frames.
         * @param msPerFrame the number of ms to display each frame.
         * @param loop if false, the sequencer will report the end of
         * the animation after progressing through all of the frames once,
         * otherwise it will loop indefinitely.
         */
        public MultiFunction (int[] sequence, long msPerFrame, boolean loop)
        {
            _length = sequence.length;
            _sequence = sequence;
            _delays = new long[_length];
            Arrays.fill(_delays, msPerFrame);
            _marks = new boolean[_length];
            _loop = loop;
        }

        /**
         * Creates a fully-specified multifunction frame sequencer.
         *
         * @param sequence the ordering to display the frames.
         * @param msPerFrame the number of ms to display each frame.
         * @param marks if true for a frame, a FrameReachedEvent will
         * be generated when that frame is reached.
         * @param loop if false, the sequencer will report the end of
         * the animation after progressing through all of the frames once,
         * otherwise it will loop indefinitely.
         */
        public MultiFunction (
            int[] sequence, long[] msPerFrame, boolean[] marks, boolean loop)
        {
            _length = sequence.length;
            if ((_length != msPerFrame.length) || (_length != marks.length)) {
                throw new IllegalArgumentException(
                    "All MultiFunction FrameSequencer arrays must be " +
                    "the same length.");
            }

            _sequence = sequence;
            _delays = msPerFrame;
            _marks = marks;
            _loop = loop;
        }

        // documentation inherited from interface
        public void init (MultiFrameImage source)
        {
            int framecount = source.getFrameCount();
            // let's make sure our frames are valid
            for (int ii=0; ii < _length; ii++) {
                if (_sequence[ii] >= framecount) {
                    throw new IllegalArgumentException(
                        "MultiFunction FrameSequencer was initialized " +
                        "with a MultiFrameImage with not enough frames " +
                        "to match the sequence it was constructed with.");
                }
            }

            _nextStamp = 0L;
            _curIdx = 0;
        }

        // documentation inherited from interface
        public void setAnimation (Animation anim)
        {
            _animation = anim;
        }

        // documentation inherited from interface
        public int tick (long tickStamp)
        {
            // obtain our starting timestamp if we don't already have one
            if (_nextStamp == 0L) {
                _nextStamp = tickStamp + _delays[_curIdx];
                // we might need to notify on the first frame
                checkNotify(tickStamp);
            }

            // we may have rushed through more than one frame since the last
            // tick, but we want to always notify even if we never displayed
            while (tickStamp >= _nextStamp) {
                _curIdx++;
                if (_curIdx == _length) {
                    if (_loop) {
                        _curIdx = 0;
                    } else {
                        return -1;
                    }
                }
                checkNotify(tickStamp);
                _nextStamp += _delays[_curIdx];
            }

            // return the right frame
            return _sequence[_curIdx];
        }

        // documentation inherited from interface
        public void fastForward (long timeDelta)
        {
            // this method should be called "unpause"
            _nextStamp += timeDelta;
        }

        /**
         * Check to see if we need to notify that we've reached a marked frame.
         */
        protected void checkNotify (long tickStamp)
        {
            if (_marks[_curIdx]) {
                _animation.notifyObservers(
                    new FrameReachedEvent(
                        _animation, tickStamp, _sequence[_curIdx], _curIdx));
            }
        }

        /** The current sequence index. */
        protected int _curIdx = 0;

        /** The length of our animation sequence. */
        protected int _length;

        /** The sequence of frames to display. */
        protected int[] _sequence;

        /** The corresponding delay for each frame, in ms. */
        protected long[] _delays;

        /** Whether to send a FrameReachedEvent on a sequence index. */
        protected boolean[] _marks;

        /** Does the animation loop? */
        protected boolean _loop;

        /** The time at which we'll switch to the next frame. */
        protected long _nextStamp;

        /** The animation that we're sequencing for. */
        protected Animation _animation;
    }
}
