//
// $Id: ChatSender.java,v 1.2 2002/08/20 19:38:13 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.ChatDecoder;
import com.threerings.crowd.chat.ChatReceiver;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link ChatReceiver} instance on a
 * client.
 *
 * <p> Generated from <code>
 * $Id: ChatSender.java,v 1.2 2002/08/20 19:38:13 mdb Exp $
 * </code>
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

    // Generated on 12:36:59 08/20/02.
}
