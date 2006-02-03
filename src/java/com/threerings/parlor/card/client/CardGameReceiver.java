//
// $Id: ParlorReceiver.java 3099 2004-08-27 02:21:06Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;

import com.threerings.presents.client.InvocationReceiver;

/**
 * Defines, for the card game services, a set of notifications delivered
 * asynchronously by the server to the client.
 */
public interface CardGameReceiver extends InvocationReceiver
{
    /**
     * Dispatched to the client when it has received a hand of cards.
     *
     * @param oid the oid of the game for which this hand applies
     * @param hand the received hand
     */
    public void receivedHand (int oid, Hand hand);
    
    /**
     * Dispatched to the client when it has received a set of cards
     * from another player.
     *
     * @param plidx the index of the player providing the cards
     * @param cards the cards received
     */
    public void receivedCardsFromPlayer (int plidx, Card[] cards);
    
    /**
     * Dispatched to the client when the server has forced it to send
     * a set of cards to another player.
     *
     * @param plidx the index of the player to which the cards were sent
     * @param cards the cards sent
     */
    public void sentCardsToPlayer (int plidx, Card[] cards);
    
    /**
     * Dispatched to the client when a set of cards is transferred between
     * two other players in the game.
     *
     * @param fromidx the index of the player sending the cards
     * @param toidx the index of the player receiving the cards
     * @param cards the number of cards transferred
     */
    public void cardsTransferredBetweenPlayers (int fromidx, int toidx,
        int cards);
}
