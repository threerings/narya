package com.threerings.presents.data {

import com.threerings.util.ClassUtil;

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
