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
 * Provides a pluggable mechanism for delivering high resolution timing
 * information. The timers are not intended to be used by different
 * threads and thus must be protected by synchronization in such
 * circumstances.
 */
public interface MediaTimer
{
    /**
     * Resets the timer's monotonically increasing value.
     */
    public void reset ();

    /**
     * Returns the number of milliseconds that have elapsed since the
     * timer was created or last {@link #reset}. <em>Note:</em> the
     * accuracy of this method is highly dependent on the timer
     * implementation used.
     */
    public long getElapsedMillis ();

    /**
     * Returns the number of microseconds that have elapsed since the
     * timer was created or last {@link #reset}. <em>Note:</em> the
     * accuracy of this method is highly dependent on the timer
     * implementation used.
     */
    public long getElapsedMicros ();
}
