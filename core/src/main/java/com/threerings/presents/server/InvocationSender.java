//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.InvocationNotificationEvent;
import com.threerings.presents.net.Transport;

import static com.threerings.presents.Log.log;

/**
 * Provides basic functionality used by all invocation sender classes.
 */
public abstract class InvocationSender
{
    /**
     * Requests that the specified invocation notification be packaged up and sent to the supplied
     * target client.
     */
    public static void sendNotification (
        ClientObject target, String receiverCode, int methodId, Object[] args)
    {
        sendNotification(target, receiverCode, methodId, args, Transport.DEFAULT);
    }

    /**
     * Requests that the specified invocation notification be packaged up and sent to the supplied
     * target client.
     */
    public static void sendNotification (
        ClientObject target, String receiverCode, int methodId, Object[] args, Transport transport)
    {
        // convert the receiver hash id into the code used on this
        // specific client
        Registration rreg = target.receivers.get(receiverCode);
        if (rreg == null) {
            log.warning("Unable to locate receiver for invocation service notification",
                        "clobj", target.who(), "code", receiverCode, "methId", methodId,
                        "args", args, new Exception());

        } else {
//             log.info("Sending notification", "target", target, "code", receiverCode,
//                      "methodId", methodId, "args", args);

            // create and dispatch an invocation notification event
            target.postEvent(new InvocationNotificationEvent(
                                 target.getOid(), rreg.receiverId, methodId, args).
                             setTransport(transport));
        }
    }
}
