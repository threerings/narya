//
// $Id: InvocationDecoder.java,v 1.1 2002/08/14 19:07:54 mdb Exp $

package com.threerings.presents.client;

import com.samskivert.util.StringUtil;
import com.threerings.presents.Log;

/**
 * Provides the basic functionality used to dispatch invocation
 * notification events.
 */
public abstract class InvocationDecoder
{
    /** The receiver for which we're decoding and dipatching
     * notifications. */
    public InvocationReceiver receiver;

    /**
     * Returns the generated hash code that is used to identify this
     * invocation notification service.
     */
    public abstract String getReceiverCode ();

    /**
     * Dispatches the specified method to our receiver.
     */
    public void dispatchNotification (int methodId, Object[] args)
    {
        Log.warning("Requested to dispatch unknown method " +
                    "[receiver=" + receiver + ", methodId=" + methodId +
                    ", args=" + StringUtil.toString(args) + "].");
    }
}
