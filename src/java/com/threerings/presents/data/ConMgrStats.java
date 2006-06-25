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

package com.threerings.presents.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Used to track and report stats on the connection manager.
 */
public class ConMgrStats extends SimpleStreamableObject
{
    /** The current index into the history arrays. */
    public int current;

    /** The size of the queue of waiting to auth sockets. */
    public int[] authQueueSize;

    /** The size of the queue of waiting to die sockets. */
    public int[] deathQueueSize;

    /** The outgoing queue size. */
    public int[] outQueueSize;

    /** The overflow queue size. */
    public int[] overQueueSize;

    /** The number of bytes read. */
    public int[] bytesIn;

    /** The number of bytes written. */
    public int[] bytesOut;

    /** The number of messages read. */
    public int[] msgsIn;

    /** The number of messages written. */
    public int[] msgsOut;

    /** Creates our historical arrays. */
    public void init ()
    {
        authQueueSize = new int[SLOTS];
        deathQueueSize = new int[SLOTS];
        outQueueSize = new int[SLOTS];
        overQueueSize = new int[SLOTS];
        bytesIn = new int[SLOTS];
        bytesOut = new int[SLOTS];
        msgsIn = new int[SLOTS];
        msgsOut = new int[SLOTS];
    }

    /** Advances the currently accumulating bucket and clears its
     * previous contents. */
    public void increment ()
    {
        current = (current + 1) % authQueueSize.length;
        authQueueSize[current] = 0;
        deathQueueSize[current] = 0;
        outQueueSize[current] = 0;
        overQueueSize[current] = 0;
        bytesIn[current] = 0;
        bytesOut[current] = 0;
        msgsIn[current] = 0;
        msgsOut[current] = 0;
    }

    /**
     * Returns the index of the most recently accumulated stats slot.
     */
    public int mostRecent ()
    {
        return (current + SLOTS - 1) % SLOTS;
    }

    protected static final int SLOTS = 60;
}
