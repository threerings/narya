//
// $Id: ChatDecoder.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.ChatReceiver;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link ChatReceiver} instance.
 */
public class ChatDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "50d26fa68e407846184dc06b78db3e1d";

    /** The method id used to dispatch {@link ChatReceiver#receivedTell}
     * notifications. */
    public static final int RECEIVED_TELL = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public ChatDecoder (ChatReceiver receiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    public void dispatchNotification (int methodId, Object[] args)
    {
        switch (methodId) {
        case RECEIVED_TELL:
            ((ChatReceiver)receiver).receivedTell(
                (String)args[0], (String)args[1], (String)args[2]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }

    // Generated on 11:25:46 08/12/02.
}
