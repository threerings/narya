//
// $Id$

package com.threerings.parlor.card.client;

import com.threerings.parlor.card.client.CardGameReceiver;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link CardGameReceiver} instance.
 */
public class CardGameDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "0718199d459e31d8d673744c71b0e788";

    /** The method id used to dispatch {@link CardGameReceiver#receivedHand}
     * notifications. */
    public static final int RECEIVED_HAND = 1;

    /** The method id used to dispatch {@link CardGameReceiver#receivedCardsFromPlayer}
     * notifications. */
    public static final int RECEIVED_CARDS_FROM_PLAYER = 2;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public CardGameDecoder (CardGameReceiver receiver)
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
            ((CardGameReceiver)receiver).receivedHand(
                (Hand)args[0]
            );
            return;

        case RECEIVED_CARDS_FROM_PLAYER:
            ((CardGameReceiver)receiver).receivedCardsFromPlayer(
                ((Integer)args[0]).intValue(), (Card[])args[1]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
