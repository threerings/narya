//
// $Id: InvocationDispatcher.java,v 1.1 2002/08/14 19:07:56 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the base class via which invocation service requests are
 * dispatched.
 */
public abstract class InvocationDispatcher
{
    /** The invocation provider for whom we're dispatching. */
    public InvocationProvider provider;

    /**
     * Creates an instance of the appropriate {@link InvocationMarshaller}
     * derived class for use with this dispatcher.
     */
    public abstract InvocationMarshaller createMarshaller ();

    /**
     * Dispatches the specified method to our provider.
     */
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        Log.warning("Requested to dispatch unknown method " +
                    "[provider=" + provider +
                    ", sourceOid=" + source.getOid() +
                    ", methodId=" + methodId +
                    ", args=" + StringUtil.toString(args) + "].");
    }
}
