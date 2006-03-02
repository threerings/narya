package com.threerings.presents.data {

import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.client.InvocationListener;

import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.InvocationResponseEvent;

import com.threerings.presents.Log;

public class ListenerMarshaller
    implements Streamable, InvocationListener
{
    /** The method id used to dispatch a requestFailed response. */
    public const REQUEST_FAILED_RSPID :int = 0;

    /** The oid of the invocation service requester. */
    public var callerOid :int;

    /** The request id associated with this listener. */
    public var requestId :int;

    /** The actual invocation listener associated with this
     * marshalling listener. This is only valid on the client. */
    public var listener :InvocationListener;

    /** The time at which this listener marshaller was registered.
     * This is only valid on the client. */
    public var mapStamp :Number;

    /** The distributed object manager to use when dispatching proxied
     * responses. This is only valid on the server. */
    public var omgr :DObjectManager;

    // documentation inherited from interface
    public function requestFailed (cause :String) :void
    {
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, REQUEST_FAILED_RSPID,
                           [ cause ]));
    }

    /**
     * Called to dispatch an invocation response to our target
     * listener.
     */
    public function dispatchResponse (methodId :int, args :Array) :void
    {
        if (methodId == REQUEST_FAILED_RSPID) {
            listener.requestFailed((args[0] as String));

        } else {
            Log.warning("Requested to dispatch unknown invocation " +
                        "response [listener=" + listener +
                        ", methodId=" + methodId +
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
    public function readObject (ins :ObjectInputStream) :void
    {
        callerOid = ins.readInt();
        requestId = ins.readShort();
    }
}
}
