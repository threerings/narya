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

package com.threerings.media.util;

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
            _millisPerFrame = (long)(1000d / framesPerSecond);
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
            return (_loop || frameIdx < _frameCount) ? (frameIdx % _frameCount)
                                                     : -1;
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
