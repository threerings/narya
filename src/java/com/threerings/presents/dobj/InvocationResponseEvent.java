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

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

/**
 * Used to dispatch an invocation response from the server to the client.
 *
 * @see DObjectManager#postEvent
 */
public class InvocationResponseEvent extends DEvent
{
    /**
     * Constructs a new invocation response event on the specified target
     * object with the supplied code, method and arguments.
     *
     * @param targetOid the object id of the object on which the event is
     * to be dispatched.
     * @param requestId the id of the request to which we are responding.
     * @param methodId the method to be invoked.
     * @param args the arguments for the method. This array should contain
     * only values of valid distributed object types.
     */
    public InvocationResponseEvent (
        int targetOid, int requestId, int methodId, Object[] args)
    {
        super(targetOid);
        _requestId = (short)requestId;
        _methodId = (byte)methodId;
        _args = args;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public InvocationResponseEvent ()
    {
    }

    /**
     * Returns the invocation request id associated with this response.
     */
    public int getRequestId ()
    {
        return _requestId;
    }

    /**
     * Returns the method associated with this response.
     */
    public int getMethodId ()
    {
        return _methodId;
    }

    /**
     * Returns the arguments associated with this response.
     */
    public Object[] getArgs ()
    {
        return _args;
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        // nothing to do here
    }

    // documentation inherited
    protected void toString (StringBuilder buf)
    {
        buf.append("IRSP:");
        super.toString(buf);
        buf.append(", reqid=").append(_requestId);
        buf.append(", methodId=").append(_methodId);
        buf.append(", args=").append(StringUtil.toString(_args));
    }

    /** The id of the request with which this response is associated. */
    protected short _requestId;

    /** The id of the method being invoked. */
    protected byte _methodId;

    /** The arguments to the method being invoked. */
    protected Object[] _args;
}
