//
// $Id: InvocationNotificationEvent.java,v 1.1 2002/08/14 19:07:55 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Used to dispatch an invocation notification from the server to a
 * client.
 *
 * @see DObjectManager#postEvent
 */
public class InvocationNotificationEvent extends DEvent
{
    /**
     * Constructs a new invocation notification event on the specified
     * target object with the supplied receiver id, method id and
     * arguments.
     *
     * @param targetOid the object id of the object on which the event is
     * to be dispatched.
     * @param receiverId identifies the receiver to which this notification
     * is being dispatched.
     * @param methodId the id of the method to be invoked.
     * @param args the arguments for the method. This array should contain
     * only values of valid distributed object types.
     */
    public InvocationNotificationEvent (
        int targetOid, short receiverId, int methodId, Object[] args)
    {
        super(targetOid);
        _receiverId = receiverId;
        _methodId = (byte)methodId;
        _args = args;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public InvocationNotificationEvent ()
    {
    }

    /**
     * Returns the receiver id associated with this notification.
     */
    public int getReceiverId ()
    {
        return _receiverId;
    }

    /**
     * Returns the id of the method associated with this notification.
     */
    public int getMethodId ()
    {
        return _methodId;
    }

    /**
     * Returns the arguments associated with this notification.
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
        out.writeShort(_receiverId);
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
        _receiverId = in.readShort();
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
        buf.append("INOT:");
        super.toString(buf);
        buf.append(", rcvId=").append(_receiverId);
        buf.append(", methodId=").append(_methodId);
        buf.append(", args=").append(StringUtil.toString(_args));
    }

    /** Identifies the receiver to which this notification is being
     * dispatched. */
    protected short _receiverId;

    /** The id of the receiver method being invoked. */
    protected byte _methodId;

    /** The arguments to the receiver method being invoked. */
    protected Object[] _args;
}
