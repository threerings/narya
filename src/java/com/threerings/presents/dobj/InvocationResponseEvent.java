//
// $Id: InvocationResponseEvent.java,v 1.1 2002/08/14 19:07:55 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

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

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeShort(_requestId);
        out.writeByte(_methodId);
        out.writeObject(_args);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _requestId = in.readShort();
        _methodId = in.readByte();
        _args = (Object[])in.readObject();
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        // nothing to do here
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
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
