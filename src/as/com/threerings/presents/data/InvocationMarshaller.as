//
// $Id: InvocationMarshaller.java 3832 2006-02-04 03:49:53Z ray $
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

package com.threerings.presents.data {

import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ConfirmListener;
import com.threerings.presents.client.ResultListener;
import com.threerings.presents.client.InvocationService;

import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides a base from which all invocation service marshallers extend.
 * Handles functionality common to all marshallers.
 */
public class InvocationMarshaller
    implements Streamable, InvocationService
{
    /**
     * Initializes this invocation marshaller instance with the requisite
     * information to allow it to operate in the wide world. This is
     * called by the invocation manager when an invocation provider is
     * registered and should not be called otherwise.
     */
    public function init (invOid :int, invCode :int) :void
    {
        _invOid = invOid;
        _invCode = invCode;
    }

    /**
     * Sets the invocation oid to which this marshaller should send its
     * invocation service requests. This is called by the invocation
     * manager in certain initialization circumstances.
     */
    public function setInvocationOid (invOid :int) :void
    {
        _invOid = invOid;
    }

    /**
     * Returns the code assigned to this marshaller.
     */
    public function getInvocationCode () :int
    {
        return _invCode;
    }

    /**
     * Called by generated invocation marshaller code; packages up and
     * sends the specified invocation service request.
     */
    protected function sendRequest (
            client :Client, methodId :int, args :Array) :void
    {
        client.getInvocationDirector().sendRequest(
            _invOid, _invCode, methodId, args);
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return "[invOid=" + _invOid + ", code=" + _invCode +
            ", type=" + ClassUtil.getClassName(this) + "]";
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(_invOid);
        out.writeInt(_invCode);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _invOid = ins.readInt();
        _invCode = ins.readInt();
    }

    /** The oid of the invocation object, where invocation service
     * requests are sent. */
    protected var _invOid :int;

    /** The invocation service code assigned to this service when it was
     * registered on the server. */
    protected var _invCode :int;
}
}
