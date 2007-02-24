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

package com.threerings.presents.client {

import com.threerings.util.Comparable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DSet_Entry;

/**
 * Used to maintain a registry of invocation receivers that can be
 * used to convert (large) hash codes into (small) registration
 * numbers.
 */
public class InvocationReceiver_Registration
    implements DSet_Entry
{
    /** The unique hash code associated with this invocation receiver class. */
    public var receiverCode :String;

    /** The unique id assigned to this invocation receiver class at
     * registration time. */
    public var receiverId :int;

    /** Creates and initializes a registration instance. */
    public function InvocationReceiver_Registration (
            receiverCode :String = null, receiverId :int = 0)
    {
        this.receiverCode = receiverCode;
        this.receiverId = receiverId;
    }

    // documentation inherited from interface DSet_Entry
    public function getKey () :Object
    {
        return receiverCode;
    }

    // documentation inherited
    public function toString () :String
    {
        return "[" + receiverCode + " => " + receiverId + "]";
    }

    // documentation inherited from superinterface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeField(receiverCode);
        out.writeShort(receiverId);
    }

    // documentation inherited from superinterface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        receiverCode = (ins.readField(String) as String);
        receiverId = ins.readShort();
    }
}
}
