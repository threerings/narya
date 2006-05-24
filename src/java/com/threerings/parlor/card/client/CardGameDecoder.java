//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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

    /** The method id used to dispatch {@link CardGameReceiver#cardsTransferredBetweenPlayers}
     * notifications. */
    public static final int CARDS_TRANSFERRED_BETWEEN_PLAYERS = 1;

    /** The method id used to dispatch {@link CardGameReceiver#receivedCardsFromPlayer}
     * notifications. */
    public static final int RECEIVED_CARDS_FROM_PLAYER = 2;

    /** The method id used to dispatch {@link CardGameReceiver#receivedHand}
     * notifications. */
    public static final int RECEIVED_HAND = 3;

    /** The method id used to dispatch {@link CardGameReceiver#sentCardsToPlayer}
     * notifications. */
    public static final int SENT_CARDS_TO_PLAYER = 4;

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
        case CARDS_TRANSFERRED_BETWEEN_PLAYERS:
            ((CardGameReceiver)receiver).cardsTransferredBetweenPlayers(
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue()
            );
            return;

        case RECEIVED_CARDS_FROM_PLAYER:
            ((CardGameReceiver)receiver).receivedCardsFromPlayer(
                ((Integer)args[0]).intValue(), (Card[])args[1]
            );
            return;

        case RECEIVED_HAND:
            ((CardGameReceiver)receiver).receivedHand(
                ((Integer)args[0]).intValue(), (Hand)args[1]
            );
            return;

        case SENT_CARDS_TO_PLAYER:
            ((CardGameReceiver)receiver).sentCardsToPlayer(
                ((Integer)args[0]).intValue(), (Card[])args[1]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
