//
// $Id: InvocationRequestEvent.java,v 1.2 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

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
    public InvocationRequestEvent (
        int targetOid, int invCode, int methodId, Object[] args)
    {
        super(targetOid);
        _invCode = invCode;
        _methodId = (byte)methodId;
        _args = args;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public InvocationRequestEvent ()
    {
    }

    /**
     * Returns the invocation code associated with this request.
     */
    public int getInvCode ()
    {
        return _invCode;
    }

    /**
     * Returns the id of the method associated with this request.
     */
    public int getMethodId ()
    {
        return _methodId;
    }

    /**
     * Returns the arguments associated with this request.
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
    protected void toString (StringBuffer buf)
    {
        buf.append("IREQ:");
        super.toString(buf);
        buf.append(", code=").append(_invCode);
        buf.append(", methodId=").append(_methodId);
        buf.append(", args=").append(StringUtil.toString(_args));
    }

    /** The code identifying which invocation provider to which this
     * request is directed. */
    protected int _invCode;

    /** The id of the method being invoked. */
    protected byte _methodId;

    /** The arguments to the method being invoked. */
    protected Object[] _args;
}
