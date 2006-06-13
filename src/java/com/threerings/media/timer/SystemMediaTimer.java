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

package com.threerings.media.timer;

/**
 * Implements the {@link MediaTimer} interface using {@link
 * System#currentTimeMillis} to obtain timing information.
 *
 * <p> <em>Note:</em> {@link System#currentTimeMillis} is notoriously
 * inaccurate on different platforms. See <a
 * href="http://developer.java.sun.com/developer/bugParade/bugs/4486109.html">
 * bug report 4486109</a> for more information.
 *
 * <p> <em>Also note:</em> clock drift on Windows XP is especially
 * pronounced and is exacerbated by the fact that WinXP periodically
 * resyncs the system clock with the hardware clock, causing discontinuous
 * jumps in the progression of time (usually backwards in time).
 */
public class SystemMediaTimer implements MediaTimer
{
    // documentation inherited from interface
    public void reset ()
    {
        _resetStamp = System.currentTimeMillis();
    }

    // documentation inherited from interface
    public long getElapsedMillis ()
    {
        return System.currentTimeMillis() - _resetStamp;
    }

    // documentation inherited from interface
    public long getElapsedMicros ()
    {
        return getElapsedMillis() * 10;
    }

    /** The time at which this timer was last reset. */
    protected long _resetStamp = System.currentTimeMillis();
}
