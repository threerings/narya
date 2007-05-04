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

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.Log;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.client.InvocationService;

import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides a base from which all invocation service marshallers extend.  Handles functionality
 * common to all marshallers.
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
        /** The method id used to dispatch a {@link #requestFailed} response. */
        public static final int REQUEST_FAILED_RSPID = 0;

        /** The oid of the invocation service requester. */
        public int callerOid;

        /** The request id associated with this listener. */
        public short requestId;

        /** The actual invocation listener associated with this marshalling listener. This is only
         * valid on the client. */
        public transient InvocationListener listener;

        /** The time at which this listener marshaller was registered.  This is only valid on the
         * client. */
        public transient long mapStamp;

        /** The distributed object manager to use when dispatching proxied responses. This is only
         * valid on the server. */
        public transient DObjectManager omgr;

        /**
         * Set an identifier for the invocation that this listener is used for, so we can report it
         * if we are never responded-to.
         */
        public void setInvocationId (String name)
        {
            _invId = name;
        }

        /**
         * Indicates that this listener will not be responded-to, and that this is normal behavior.
         */
        public void setNoResponse ()
        {
            // we enact this by merely doing the same thing that we normally do during a response.
            _invId = null;
        }

        // documentation inherited from interface
        public void requestFailed (String cause)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_FAILED_RSPID, new Object[] { cause }));
        }

        /**
         * Called to dispatch an invocation response to our target listener.
         */
        public void dispatchResponse (int methodId, Object[] args)
        {
            if (methodId == REQUEST_FAILED_RSPID) {
                listener.requestFailed((String)args[0]);

            } else {
                Log.warning("Requested to dispatch unknown invocation response " +
                            "[listener=" + listener + ", methodId=" + methodId +
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

        // documentation inherited
        protected void finalize ()
            throws Throwable
        {
            if (_invId != null && getClass() != ListenerMarshaller.class) {
                Log.warning("Invocation listener never responded to: " + _invId);
            }
            super.finalize();
        }

        /** On the server, the id of the invocation method. */
        protected transient String _invId; 
    }

    /**
     * Defines a marshaller for the standard {@link ConfirmListener}.
     */
    public static class ConfirmMarshaller extends ListenerMarshaller
        implements ConfirmListener
    {
        /** The method id used to dispatch {@link #requestProcessed} responses. */
        public static final int REQUEST_PROCESSED = 1;

        // documentation inherited from interface
        public void requestProcessed ()
        {
            _invId = null;
            omgr.postEvent(
                new InvocationResponseEvent(callerOid, requestId, REQUEST_PROCESSED, null));
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
        /** The method id used to dispatch {@link #requestProcessed} responses. */
        public static final int REQUEST_PROCESSED = 1;

        // documentation inherited from interface
        public void requestProcessed (Object result)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_PROCESSED, new Object[] { result }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case REQUEST_PROCESSED:
                ((ResultListener)listener).requestProcessed(args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /**
     * Initializes this invocation marshaller instance with the requisite information to allow it
     * to operate in the wide world. This is called by the invocation manager when an invocation
     * provider is registered and should not be called otherwise.
     */
    public void init (int invOid, int invCode)
    {
        _invOid = invOid;
        _invCode = invCode;
    }

    /**
     * Sets the invocation oid to which this marshaller should send its invocation service
     * requests. This is called by the invocation manager in certain initialization circumstances.
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
     * A convenience method to indicate that the listener is not going to be responded-to, and that
     * this is ok.
     */
    public static void setNoResponse (InvocationListener listener)
    {
        if (listener instanceof ListenerMarshaller) {
            ((ListenerMarshaller) listener).setNoResponse();
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[invOid=" + _invOid + ", code=" + _invCode + ", type=" + getClass().getName() + "]";
    }

    // AUTO-GENERATED: METHODS START
    // from interface Streamable
    public void readObject (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        _invOid = ins.readInt();
        _invCode = ins.readInt();
    }

    // from interface Streamable
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_invOid);
        out.writeInt(_invCode);
    }
    // AUTO-GENERATED: METHODS END

    /**
     * Called by generated invocation marshaller code; packages up and sends the specified
     * invocation service request.
     */
    protected void sendRequest (Client client, int methodId, Object[] args)
    {
        client.getInvocationDirector().sendRequest(_invOid, _invCode, methodId, args);
    }

    /** The oid of the invocation object, where invocation service requests are sent. */
    protected int _invOid;

    /** The invocation service code assigned to this service when it was registered on the
     * server. */
    protected int _invCode;
}
