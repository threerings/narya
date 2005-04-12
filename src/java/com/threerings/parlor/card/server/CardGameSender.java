//
// $Id$

package com.threerings.parlor.card.server;

import com.threerings.parlor.card.client.CardGameDecoder;
import com.threerings.parlor.card.client.CardGameReceiver;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link CardGameReceiver} instance on a
 * client.
 */
public class CardGameSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * CardGameReceiver#receivedHand} on a client.
     */
    public static void sendHand (
        ClientObject target, Hand arg1)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.RECEIVED_HAND,
            new Object[] { arg1 });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * CardGameReceiver#receivedCardsFromPlayer} on a client.
     */
    public static void sendCardsFromPlayer (
        ClientObject target, int arg1, Card[] arg2)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.RECEIVED_CARDS_FROM_PLAYER,
            new Object[] { new Integer(arg1), arg2 });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * CardGameReceiver#sentCardsToPlayer} on a client.
     */
    public static void sentCardsToPlayer (
        ClientObject target, int arg1, Card[] arg2)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.SENT_CARDS_TO_PLAYER,
            new Object[] { new Integer(arg1), arg2 });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * CardGameReceiver#cardsTransferredBetweenPlayers} on a client.
     */
    public static void cardsTransferredBetweenPlayers (
        ClientObject target, int arg1, int arg2, int arg3)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.CARDS_TRANSFERRED_BETWEEN_PLAYERS,
            new Object[] { new Integer(arg1), new Integer(arg2), new Integer(arg3) });
    }

}
