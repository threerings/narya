//
// $Id: InvocationSender.java,v 1.1 2002/08/14 19:07:56 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.InvocationNotificationEvent;

/**
 * Provides basic functionality used by all invocation sender classes.
 */
public abstract class InvocationSender
{
    /**
     * Requests that the specified invocation notification be packaged up
     * and sent to the supplied target client.
     */
    public static void sendNotification (
        ClientObject target, String receiverCode, int methodId, Object[] args)
    {
        // convert the receiver hash id into the code used on this
        // specific client
        Registration rreg = (Registration)target.receivers.get(receiverCode);
        if (rreg == null) {
            Log.warning("Unable to locate registered receiver for " +
                        "invocation service notification [target=" + target +
                        ", code=" + receiverCode + ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");

        } else {
//             Log.info("Sending notification [target=" + target +
//                      ", code=" + receiverCode + ", methodId=" + methodId +
//                      ", args=" + StringUtil.toString(args) + "].");

            // create and dispatch an invocation notification event
            target.postEvent(
                new InvocationNotificationEvent(
                    target.getOid(), rreg.receiverId, methodId, args));
        }
    }
}
