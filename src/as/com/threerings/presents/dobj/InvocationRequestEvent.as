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

package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.StringBuilder;

/**
 * Used to dispatch an invocation request from the client to the server.
 *
 * @see DObjectManager#postEvent
 */
public class InvocationRequestEvent extends DEvent
{
    /**
     * Constructs a new invocation request event on the specified target
     * object with the supplied code, method and arguments.
     *
     * @param targetOid the object id of the object on which the event is
     * to be dispatched.
     * @param invCode the invocation provider identification code.
     * @param methodId the id of the method to be invoked.
     * @param args the arguments for the method. This array should contain
     * only values of valid distributed object types.
     */
    public function InvocationRequestEvent (
            targetOid :int = 0, invCode :int = 0, methodId :int = 0,
            args :Array = null)
    {
        super(targetOid);
        _invCode = invCode;
        _methodId = methodId;
        _args = args;
    }

    /**
     * Returns the invocation code associated with this request.
     */
    public function getInvCode () :int
    {
        return _invCode;
    }

    /**
     * Returns the id of the method associated with this request.
     */
    public function getMethodId () :int
    {
        return _methodId;
    }

    /**
     * Returns the arguments associated with this request.
     */
    public function getArgs () :Array
    {
        return _args;
    }

    /**
     * Applies this attribute change to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        // nothing to do here
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("IREQ:");
        super.toStringBuf(buf);
        buf.append(", code=", _invCode);
        buf.append(", methodId=", _methodId);
        buf.append(", args=", _args);
    }

    // documentation inherited from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(_invCode);
        out.writeByte(_methodId);
        out.writeField(_args);
    }

    // documentation inherited from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _invCode = ins.readInt();
        _methodId = ins.readByte();
        _args = (ins.readField(Array) as Array);
    }

    /** The code identifying which invocation provider to which this
     * request is directed. */
    protected var _invCode :int;

    /** The id of the method being invoked. */
    protected var _methodId :int;

    /** The arguments to the method being invoked. */
    protected var _args :Array;
}
}
