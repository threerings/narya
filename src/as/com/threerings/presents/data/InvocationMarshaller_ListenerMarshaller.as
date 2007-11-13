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

import com.threerings.util.ClassUtil;
import com.threerings.util.Log;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.client.InvocationService_InvocationListener;

import com.threerings.presents.dobj.InvocationResponseEvent;

public class InvocationMarshaller_ListenerMarshaller
    implements Streamable
{
    /** The method id used to dispatch a requestFailed response. */
    public static const REQUEST_FAILED_RSPID :int = 0;

    /** The oid of the invocation service requester. */
    public var callerOid :int;

    /** The request id associated with this listener. */
    public var requestId :int;

    /** The actual invocation listener associated with this
     * marshalling listener. This is only valid on the client. */
    public var listener :InvocationService_InvocationListener;

    /** The time at which this listener marshaller was registered.
     * This is only valid on the client. */
    public var mapStamp :Number;

    /**
     * Called to dispatch an invocation response to our target
     * listener.
     */
    public function dispatchResponse (methodId :int, args :Array) :void
    {
        if (methodId == REQUEST_FAILED_RSPID) {
            listener.requestFailed((args[0] as String));

        } else {
            Log.getLog(this).warning(
                "Requested to dispatch unknown invocation response " + 
                "[listener=" + listener + ", methodId=" + methodId +
                ", args=" + args + "].");
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return "[callerOid=" + callerOid + ", reqId=" + requestId +
            ", type=" + ClassUtil.getClassName(this) +  "]";
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(callerOid);
        out.writeShort(requestId);
    }

    // documentation inherited from interface Streamable
    public final function readObject (ins :ObjectInputStream) :void
    {
        throw new Error(); // abstract: not needed
    }
}
}
