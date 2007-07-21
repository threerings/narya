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

package com.threerings.presents.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Cloneable;
import com.threerings.util.Long;

/**
 * Used to track and report stats on the connection manager.
 */
public class ConMgrStats extends SimpleStreamableObject
    implements Cloneable
{
    /** The size of the queue of waiting to auth sockets. This is a snapshot at the time the stats
     * are requested. */
    public var authQueueSize :int;

    /** The size of the queue of waiting to die sockets. This is a snapshot at the time the stats
     * are requested. */
    public var deathQueueSize :int;

    /** The outgoing queue size. This is a snapshot at the time the stats are requested. */
    public var outQueueSize :int;

    /** The overflow queue size. This is a snapshot at the time the stats are requested. */
    public var overQueueSize :int;

    /** The number of connection events since the server started up. */
    public var connects :int;

    /** The number of disconnection events since the server started up. */
    public var disconnects :int;

    /** The number of bytes read since the server started up. */
    public var bytesIn :Long;

    /** The number of bytes written since the server started up. */
    public var bytesOut :Long;

    /** The number of messages read since the server started up. */
    public var msgsIn :int;

    /** The number of messages written since the server started up. */
    public var msgsOut :int;

    public function ConMgrStats ()
    {
        // nothing needed
    }

    // from Object
    public function clone () :Object
    {
        throw new Error("Clone not implemented. Implement if you need it.");
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        authQueueSize = ins.readInt();
        deathQueueSize = ins.readInt();
        outQueueSize = ins.readInt();
        overQueueSize = ins.readInt();
        connects = ins.readInt();
        disconnects = ins.readInt();
        bytesIn = new Long(ins.readInt(), ins.readInt());
        bytesOut = new Long(ins.readInt(), ins.readInt());
        msgsIn = ins.readInt();
        msgsOut = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(authQueueSize);
        out.writeInt(deathQueueSize);
        out.writeInt(outQueueSize);
        out.writeInt(overQueueSize);
        out.writeInt(connects);
        out.writeInt(disconnects);
        out.writeInt(bytesIn == null ? 0 : bytesIn.low);
        out.writeInt(bytesIn == null ? 0 : bytesIn.high);
        out.writeInt(bytesOut == null ? 0 : bytesOut.low);
        out.writeInt(bytesOut == null ? 0 : bytesOut.high);
        out.writeInt(msgsIn);
        out.writeInt(msgsOut);
    }
}
}
