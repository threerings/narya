//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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
    implements Cloneable
{
    /** The size of the queue of waiting to auth sockets. This is a snapshot at the time the stats
     * are requested. */
    public int authQueueSize;

    /** The size of the queue of waiting to die sockets. This is a snapshot at the time the stats
     * are requested. */
    public int deathQueueSize;

    /** The outgoing queue size. This is a snapshot at the time the stats are requested. */
    public int outQueueSize;

    /** The overflow queue size. This is a snapshot at the time the stats are requested. */
    public int overQueueSize;

    /** The number of connection events since the server started up. */
    public int connects;

    /** The number of disconnection events since the server started up. */
    public int disconnects;

    /** The number of bytes read since the server started up. */
    public long bytesIn;

    /** The number of bytes written since the server started up. */
    public long bytesOut;

    /** The number of messages read since the server started up. */
    public int msgsIn;

    /** The number of messages written since the server started up. */
    public int msgsOut;

    @Override // from Object
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }
}
