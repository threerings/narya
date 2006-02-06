//
// $Id: TimeFunction.java 3122 2004-09-18 22:57:08Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.util;

/**
 * Used to vary a value over time where time is provided at discrete increments
 * (on the frame tick) and the value is computed appropriately. This can be
 * used for fades and other effects where different functions (linear, ease-in
 * ease-out, etc.) should be easy to plug in.
 */
public abstract class TimeFunction
{
    /**
     * Every time function varies a value from some starting value to some
     * ending value over some duration. The way in which it varies
     * (linearly, for example) is up to the derived class.
     *
     * @param start the starting value.
     * @param end the ending value.
     * @param duration the duration in seconds.
     */
    public TimeFunction (float start, float end, float duration)
    {
        _start = start;
        _end = end;
        _duration = duration;
    }

    /**
     * Returns the current value given the supplied elapsed time. The value
     * will be bounded to the originally supplied starting and ending values at
     * times 0 (and below) and {@link #_duration} (and above) respectively.
     *
     * @param deltaTime the amount of time that has elapsed since the last call
     * to this method.
     */
    public float getValue (float deltaTime)
    {
        _elapsed += deltaTime;

        if (_elapsed <= 0) {
            return _start;
        } else if (_elapsed >= _duration) {
            return _end;
        } else {
            return computeValue();
        }
    }

    /**
     * Returns true if this function has proceeded the full length of its
     * duration. This should be called after a call to {@link #getValue} has
     * been made to update our internal elapsed time.
     */
    public boolean isComplete ()
    {
        return _elapsed >= _duration;
    }

    /**
     * This must be implemented by our derived class to compute our value given
     * the currently stored {@link #_elapsed} time.
     */
    protected abstract float computeValue ();

    /** Our starting and ending values. */
    protected float _start, _end;

    /** The number of milliseconds over which we vary our value. */
    protected float _duration;

    /** The number of seconds that have elapsed since we started. */
    protected float _elapsed;
}
