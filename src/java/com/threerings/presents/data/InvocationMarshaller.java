//
// $Id: InvocationMarshaller.java,v 1.1 2002/08/14 19:07:55 mdb Exp $

package com.threerings.presents.data;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
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
     * Called by generated invocation marshaller code; packages up and
     * sends the specified invocation service request.
     */
    protected void sendRequest (Client client, int methodId, Object[] args)
    {
        client.getInvocationDirector().sendRequest(
            _invOid, _invCode, methodId, args);
    }

    /**
     * Writes this instance to the supplied output stream.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_invOid);
        out.writeShort(_invCode);
    }

    /**
     * Reads this instance from the supplied input stream.
     */
    public void readObject (ObjectInputStream in)
        throws IOException
    {
        _invOid = in.readInt();
        _invCode = in.readShort();
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
