//
// $Id$

package com.threerings.parlor.card.client;

import com.threerings.parlor.card.client.CardReceiver;
import com.threerings.parlor.card.data.Hand;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link CardReceiver} instance.
 */
public class CardDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "bfa3311cedc30969d04d8b6259d6ffe0";

    /** The method id used to dispatch {@link CardReceiver#receivedHand}
     * notifications. */
    public static final int RECEIVED_HAND = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public CardDecoder (CardReceiver receiver)
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
        case RECEIVED_HAND:
            ((CardReceiver)receiver).receivedHand(
                (Hand)args[0]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
