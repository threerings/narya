//
// $Id: TestSender.java,v 1.1 2002/08/14 19:08:01 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.client.TestDecoder;
import com.threerings.presents.client.TestReceiver;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link TestReceiver} instance on a
 * client.
 */
public class TestSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * TestReceiver#receivedTest} on a client.
     */
    public static void sendTest (
        ClientObject target, int arg1, String arg2)
    {
        sendNotification(
            target, TestDecoder.RECEIVER_CODE, TestDecoder.RECEIVED_TEST,
            new Object[] { new Integer(arg1), arg2 });
    }

    // Generated on 12:14:10 08/12/02.
}
