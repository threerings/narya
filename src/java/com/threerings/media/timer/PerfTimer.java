//
// $Id: PerfTimer.java,v 1.2 2004/08/27 02:12:46 mdb Exp $
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

import sun.misc.Perf;

import com.threerings.media.Log;

/**
 * A timer that uses the performance clock exposed by Sun in JDK 1.4.2.
 */
public class PerfTimer implements MediaTimer
{
    public PerfTimer ()
    {
        _timer = Perf.getPerf();
        _frequency = _timer.highResFrequency();
        _startStamp = _timer.highResCounter() * 1000 / _frequency;
        Log.info("Using high performance timer [freq=" + _frequency +
                 ", start=" + _startStamp + "].");
    }

    // documentation inherited from interface
    public void reset ()
    {
        _startStamp = _timer.highResCounter() * 1000 / _frequency;
    }

    // documentation inherited from interface
    public long getElapsedMillis ()
    {
        return _timer.highResCounter() * 1000 / _frequency;
    }

    // documentation inherited from interface
    public long getElapsedMicros ()
    {
        return _timer.highResCounter() * 100 / _frequency;
    }

    /** A performance timer object. */
    protected Perf _timer;

    /** The time at which this timer was last reset. */
    protected long _startStamp;

    /** The timer frequency. */
    protected long _frequency;
}
