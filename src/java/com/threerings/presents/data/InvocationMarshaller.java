//
// $Id: InvocationMarshaller.java,v 1.6 2004/08/27 02:20:19 mdb Exp $
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

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

import com.threerings.presents.Log;

import com.threerings.presents.client.Client;
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
     * Provides a base from which invocation listener marshallers extend.
     */
    public static class ListenerMarshaller
        implements Streamable, InvocationListener
    {
        /** The method id used to dispatch a {@link #requestFailed}
         * response. */
        public static final int REQUEST_FAILED_RSPID = 0;

        /** The oid of the invocation service requester. */
        public int callerOid;

        /** The request id associated with this listener. */
        public short requestId;

        /** The actual invocation listener associated with this
         * marshalling listener. This is only valid on the client. */
        public transient InvocationListener listener;

        /** The time at which this listener marshaller was registered.
         * This is only valid on the client. */
        public transient long mapStamp;

        /** The distributed object manager to use when dispatching proxied
         * responses. This is only valid on the server. */
        public transient DObjectManager omgr;

        // documentation inherited from interface
        public void requestFailed (String cause)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_FAILED_RSPID,
                               new Object[] { cause }));
        }

        /**
         * Called to dispatch an invocation response to our target
         * listener.
         */
        public void dispatchResponse (int methodId, Object[] args)
        {
            if (methodId == REQUEST_FAILED_RSPID) {
                listener.requestFailed((String)args[0]);

            } else {
                Log.warning("Requested to dispatch unknown invocation " +
                            "response [listener=" + listener +
                            ", methodId=" + methodId +
                            ", args=" + StringUtil.toString(args) + "].");
            }
        }

        /**
         * Generates a string representation of this instance.
         */
        public String toString ()
        {
            return "[callerOid=" + callerOid + ", reqId=" + requestId +
                ", type=" + getClass().getName() + "]";
        }
    }

    /**
     * Defines a marshaller for the standard {@link ConfirmListener}.
     */
    public static class ConfirmMarshaller extends ListenerMarshaller
        implements ConfirmListener
    {
        /** The method id used to dispatch {@link #requestProcessed}
         * responses. */
        public static final int REQUEST_PROCESSED = 1;

        // documentation inherited from interface
        public void requestProcessed ()
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_PROCESSED,
                               null));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case REQUEST_PROCESSED:
                ((ConfirmListener)listener).requestProcessed();
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /**
     * Defines a marshaller for the standard {@link ResultListener}.
     */
    public static class ResultMarshaller extends ListenerMarshaller
        implements ResultListener
    {
        /** The method id used to dispatch {@link #requestProcessed}
         * responses. */
        public static final int REQUEST_PROCESSED = 1;

        // documentation inherited from interface
        public void requestProcessed (Object result)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_PROCESSED,
                               new Object[] { result }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case REQUEST_PROCESSED:
                ((ResultListener)listener).requestProcessed(
                    args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /**
     * Initializes this invocation marshaller instance with the requisite
     * information to allow it to operate in the wide world. This is
     * called by the invocation manager when an invocation provider is
     * registered and should not be called otherwise.
     */
    public void init (int invOid, int invCode)
    {
        _invOid = invOid;
        _invCode = invCode;
    }

    /**
     * Sets the invocation oid to which this marshaller should send its
     * invocation service requests. This is called by the invocation
     * manager in certain initialization circumstances.
     */
    public void setInvocationOid (int invOid)
    {
        _invOid = invOid;
    }

    /**
     * Returns the code assigned to this marshaller.
     */
    public int getInvocationCode ()
    {
        return _invCode;
    }

    /**
     * Called by generated invocation marshaller code; packages up and
     * sends the specified invocation service request.
     */
    protected void sendRequest (Client client, int methodId, Object[] args)
    {
        client.getInvocationDirector().sendRequest(
            _invOid, _invCode, methodId, args);
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[invOid=" + _invOid + ", code=" + _invCode +
            ", type=" + getClass().getName() + "]";
    }

    /** The oid of the invocation object, where invocation service
     * requests are sent. */
    protected int _invOid;

    /** The invocation service code assigned to this service when it was
     * registered on the server. */
    protected int _invCode;
}
