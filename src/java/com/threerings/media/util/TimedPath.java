//
// $Id: TimedPath.java,v 1.5 2004/08/27 02:12:47 mdb Exp $
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

import com.threerings.media.Log;

/**
 * A base class for path implementations that endeavor to move their
 * pathables along a path in a specified number of milliseconds.
 */
public abstract class TimedPath implements Path
{
    /**
     * Configures the timed path with the duration in which it must
     * traverse its path.
     */
    public TimedPath (long duration)
    {
        // sanity check some things
        if (duration <= 0) {
            Log.warning("Requested path with illegal duration (<=0) " +
                        "[duration=" + duration + "]");
            Thread.dumpStack();
            duration = 1; // assume something short but non-zero
        }

        _duration = duration;
    }

    // documentation inherited
    public void init (Pathable pable, long timestamp)
    {
        // give the pable a chance to perform any starting antics
        pable.pathBeginning();

        // make a note of when we started
        _startStamp = timestamp;

        // we'll be ticked immediately following init() which will update
        // our position to the start of our path
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _startStamp += timeDelta;
    }

    // documentation inherited from interface
    public void wasRemoved (Pathable pable)
    {
        // nothing doing
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An extensible method for generating a string representation of this
     * instance.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("duration=").append(_duration).append("ms");
    }

    /** The duration that we're to spend following the path. */
    protected long _duration;

    /** The time at which we started along the path. */
    protected long _startStamp;
}
