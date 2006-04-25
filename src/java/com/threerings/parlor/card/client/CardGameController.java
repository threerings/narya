//
// $Id$
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

import com.threerings.util.Name;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.card.Log;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.Hand;
import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.turn.client.TurnGameController;

/**
 * A controller class for card games.  Handles common functions like
 * accepting dealt hands.
 */
public abstract class CardGameController extends GameController
    implements TurnGameController, CardCodes, CardGameReceiver
{
    // Documentation inherited.
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        if (_ctx.getClient().getClientObject().receivers.containsKey(
                CardGameDecoder.RECEIVER_CODE)) {
            Log.warning("Yuh oh, we already have a card game receiver " +
                "registered and are trying for another...!");
            Thread.dumpStack();
        }
        
        _ctx.getClient().getInvocationDirector().registerReceiver(
            new CardGameDecoder(this));
    }
    
    // Documentation inherited.
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        _ctx.getClient().getInvocationDirector().unregisterReceiver(
            CardGameDecoder.RECEIVER_CODE);
    }
    
    // Documentation inherited.
    public void turnDidChange (Name turnHolder)
    {}

    /**
     * Called by our sender to notify us of a received hand.
     */
    public final void receivedHand (int oid, Hand hand)
    {
        if (oid == _gobj.getOid()) {
            receivedHand(hand);
        }
    }
    
    /**
     * Called when the server deals the client a new hand of cards.  Default
     * implementation does nothing.
     *
     * @param hand the hand dealt to the user
     */
    public void receivedHand (Hand hand)
    {}
    
    /**
     * Dispatched to the client when it has received a set of cards
     * from another player.  Default implementation does nothing.
     *
     * @param plidx the index of the player providing the cards
     * @param cards the cards received
     */
    public void receivedCardsFromPlayer (int plidx, Card[] cards)
    {}
    
    /**
     * Dispatched to the client when the server has forced it to send
     * a set of cards to another player.  Default implementation does
     * nothing.
     *
     * @param plidx the index of the player to which the cards were sent
     * @param cards the cards sent
     */
    public void sentCardsToPlayer (int plidx, Card[] cards)
    {}
    
    /**
     * Dispatched to the client when a set of cards is transferred between
     * two other players in the game.  Default implementation does nothing.
     *
     * @param fromidx the index of the player sending the cards
     * @param toidx the index of the player receiving the cards
     * @param cards the number of cards transferred
     */
    public void cardsTransferredBetweenPlayers (int fromidx, int toidx,
        int cards)
    {}
}
