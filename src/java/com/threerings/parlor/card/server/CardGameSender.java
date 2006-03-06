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
     * CardGameReceiver#cardsTransferredBetweenPlayers} on a client.
     */
    public static void cardsTransferredBetweenPlayers (
        ClientObject target, int arg1, int arg2, int arg3)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.CARDS_TRANSFERRED_BETWEEN_PLAYERS,
            new Object[] { new Integer(arg1), new Integer(arg2), new Integer(arg3) });
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
     * CardGameReceiver#receivedHand} on a client.
     */
    public static void sendHand (
        ClientObject target, int arg1, Hand arg2)
    {
        sendNotification(
            target, CardGameDecoder.RECEIVER_CODE, CardGameDecoder.RECEIVED_HAND,
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

}
