//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.animation;

import com.samskivert.util.ObserverList;

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
     * A sequencer that can step through a series of frames in any order
     * and speed and notify (via {@link SequencedAnimationObserver}) when
     * specific frames are reached.
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
            setDelay(msPerFrame);
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

        /**
         * Set the delay to use.
         */
        public void setDelay (long msPerFrame)
        {
            _delays = null;
            _delay = msPerFrame;
        }

        /**
         * Set the length of the sequence we are to use, or 0 means set
         * it to the length of the sequence array that we were constructed with.
         */
        public void setLength (int len)
        {
            _length = (len == 0) ? _sequence.length : len;
            if (_curIdx >= _length) {
                _curIdx = 0;
            }
        }

        /**
         * Do we reset the animation on init? Default is true.
         */
        public void setResetOnInit (boolean resets)
        {
            _resets = resets;
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

            if (_resets) {
                _lastStamp = 0L;
                _curIdx = 0;
            }
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
            if (_lastStamp == 0L) {
                _lastStamp = tickStamp;
                // we might need to notify on the first frame
                checkNotify(tickStamp);
            }

            // we may have rushed through more than one frame since the last
            // tick, but we want to always notify even if we never displayed
            long curdelay = getDelay(_curIdx);
            while (tickStamp >= (_lastStamp + curdelay)) {
                _lastStamp += curdelay;
                _curIdx++;
                if (_curIdx == _length) {
                    if (_loop) {
                        _curIdx = 0;
                    } else {
                        return -1;
                    }
                }
                checkNotify(tickStamp);

                // get the delay for checking the next frame
                curdelay = getDelay(_curIdx);
            }

            // return the right frame
            return _sequence[_curIdx];
        }

        // documentation inherited from interface
        public void fastForward (long timeDelta)
        {
            // this method should be called "unpause"
            _lastStamp += timeDelta;
        }

        /**
         * Get the delay to use for the specified frame.
         */
        protected final long getDelay (int index)
        {
            return (_delays == null) ? _delay : _delays[index];
        }

        /**
         * Check to see if we need to notify that we've reached a marked frame.
         */
        protected void checkNotify (long tickStamp)
        {
            if (_marks[_curIdx]) {
                _animation.queueNotification(
                    new FrameReachedOp(_animation, tickStamp,
                                       _sequence[_curIdx], _curIdx));
            }
        }

        /** The current sequence index. */
        protected int _curIdx = 0;

        /** The length of our animation sequence. */
        protected int _length;

        /** Do we reset on init? */
        protected boolean _resets = true;

        /** The sequence of frames to display. */
        protected int[] _sequence;

        /** The corresponding delay for each frame, in ms. */
        protected long[] _delays;

        /** Or a single delay for all frames. */
        protected long _delay;

        /** Whether to send a FrameReachedEvent on a sequence index. */
        protected boolean[] _marks;

        /** Does the animation loop? */
        protected boolean _loop;

        /** The time at which we were last ticked. */
        protected long _lastStamp;

        /** The animation that we're sequencing for. */
        protected Animation _animation;

        /** Used to dispatch {@link SequencedAnimationObserver#frameReached}. */
        protected static class FrameReachedOp implements ObserverList.ObserverOp
        {
            public FrameReachedOp (Animation anim, long when,
                                   int frameIdx, int frameSeq) {
                _anim = anim;
                _when = when;
                _frameIdx = frameIdx;
                _frameSeq = frameSeq;
            }

            public boolean apply (Object observer) {
                if (observer instanceof SequencedAnimationObserver) {
                    ((SequencedAnimationObserver)observer).frameReached(
                        _anim, _when, _frameIdx, _frameSeq);
                }
                return true;
            }

            protected Animation _anim;
            protected long _when;
            protected int _frameIdx, _frameSeq;
        }
    }
}
