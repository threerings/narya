//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.nio.conman;

import com.samskivert.util.StringUtil;

public class ConMgrStats
    implements Cloneable
{
    /** The number of mapped connections. This is a snapshot at the time the stats are requested. */
    public int connectionCount;

    /** The number of net event handlers. This is a snapshot at the time the stats are requested. */
    public int handlerCount;

    /**
     * The size of the queue of waiting to die sockets. This is a snapshot at the time the stats
     * are requested.
     */
    public int deathQueueSize;

    /** The outgoing queue size. This is a snapshot at the time the stats are requested. */
    public int outQueueSize;

    /** The overflow queue size. This is a snapshot at the time the stats are requested. */
    public int overQueueSize;

    /** The number of raw network events (sockets reporting ACCEPT or READY). */
    public long eventCount;

    /** The number of connection events since the server started up. */
    public int connects;

    /** The number of disconnection events since the server started up. */
    public int disconnects;

    /** The number of socket closes since the server started up. */
    public int closes;

    /** The number of bytes read since the server started up. */
    public long bytesIn;

    /** The number of bytes written since the server started up. */
    public long bytesOut;

    /** The number of messages read since the server started up. */
    public long msgsIn;

    /** The number of messages written since the server started up. */
    public long msgsOut;

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    @Override
    public ConMgrStats clone ()
    {
        try {
            return (ConMgrStats)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }
}
