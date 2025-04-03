//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
     * Constructs a new invocation request event on the specified target object with the supplied
     * code, method and arguments.
     *
     * @param targetOid the object id of the object on which the event is to be dispatched.
     * @param invCode the invocation provider identification code.
     * @param methodId the id of the method to be invoked.
     * @param args the arguments for the method. This array should contain only values of valid
     * distributed object types.
     */
    public InvocationRequestEvent (int targetOid, int invCode, int methodId, Object[] args)
    {
        super(targetOid);
        _invCode = invCode;
        _methodId = (byte)methodId;
        _args = args;
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

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        // nothing to do here
    }

    @Override
    protected void toString (StringBuilder buf)
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
