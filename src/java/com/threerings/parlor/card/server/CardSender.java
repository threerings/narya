//
// $Id$

package com.threerings.parlor.card.server;

import com.threerings.parlor.card.client.CardDecoder;
import com.threerings.parlor.card.client.CardReceiver;
import com.threerings.parlor.card.data.Hand;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link CardReceiver} instance on a
 * client.
 */
public class CardSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * CardReceiver#receivedHand} on a client.
     */
    public static void sendHand (
        ClientObject target, Hand arg1)
    {
        sendNotification(
            target, CardDecoder.RECEIVER_CODE, CardDecoder.RECEIVED_HAND,
            new Object[] { arg1 });
    }

}
