//
// $Id: ChatSender.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.ChatDecoder;
import com.threerings.crowd.chat.ChatReceiver;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link ChatReceiver} instance on a
 * client.
 */
public class ChatSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * ChatReceiver#receivedTell} on a client.
     */
    public static void sendTell (
        ClientObject target, String arg1, String arg2, String arg3)
    {
        sendNotification(
            target, ChatDecoder.RECEIVER_CODE, ChatDecoder.RECEIVED_TELL,
            new Object[] { arg1, arg2, arg3 });
    }

    // Generated on 11:25:46 08/12/02.
}
