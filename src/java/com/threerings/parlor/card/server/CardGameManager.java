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

package com.threerings.parlor.card.server;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.card.Log;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.CardGameMarshaller;
import com.threerings.parlor.card.data.CardGameObject;
import com.threerings.parlor.card.data.Deck;
import com.threerings.parlor.card.data.Hand;

import com.threerings.parlor.game.server.GameManager;

import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsServer;

/**
 * A manager class for card games.  Handles common functions like dealing
 * hands of cards to all players.
 */
public class CardGameManager extends GameManager
    implements CardCodes, CardGameProvider
{
    // Documentation inherited.
    protected void didStartup ()
    {
        super.didStartup();
        
        _cardgameobj = (CardGameObject)_gameobj;
        
        _cardgameobj.setCardGameService(
            (CardGameMarshaller)PresentsServer.invmgr.registerDispatcher(
                new CardGameDispatcher(this), false));
    }
    
    // Documentation inherited.
    protected void didShutdown ()
    {
        super.didShutdown();
        
        PresentsServer.invmgr.clearDispatcher(_cardgameobj.cardGameService);
    }
    
    /**
     * Deals a hand of cards to the player at the specified index from
     * the given Deck.
     *
     * @param deck the deck from which to deal
     * @param size the size of the hand to deal
     * @param playerIndex the index of the target player
     * @return the hand dealt to the player, or null if the deal
     * was canceled because the deck did not contain enough cards
     */
    public Hand dealHand (Deck deck, int size, int playerIndex)
    {
        if (deck.cards.size() < size) {
            return null;
        }
        else {
            Hand hand = deck.dealHand(size);
            
            CardGameSender.sendHand(
                (ClientObject)PresentsServer.omgr.getObject(
                    _playerOids[playerIndex]), hand);
            
            return hand;
        }
    }
    
    /**
     * Deals a hand of cards to each player from the specified
     * Deck.
     *
     * @param deck the deck from which to deal
     * @param size the size of the hands to deal
     * @return the array of hands dealt to each player, or null if
     * the deal was canceled because the deck did not contain enough
     * cards
     */
    public Hand[] dealHands (Deck deck, int size)
    {
        if (deck.cards.size() < size * _playerCount) {
            return null;
        }
        else {
            Hand[] hands = new Hand[_playerCount];
            
            for (int i=0;i<_playerCount;i++) {
                hands[i] = dealHand(deck, size, i);   
            }
            
            return hands;
        }
    }
    
    /**
     * Gets the player index of the specified client object, or -1
     * if the client object does not represent a player.
     */
    public int getPlayerIndex (ClientObject client)
    {
        int oid = client.getOid();
        for (int i=0;i<_playerOids.length;i++) {
            if (_playerOids[i] == oid) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Checks whether or not it is acceptable to transfer the given set of
     * cards from the first player to the second.  Default implementation
     * simply returns true.
     *
     * @param fromPlayerIdx the index of the sending player
     * @param toPlayerIdx the index of the receiving player
     * @param cards the cards to send
     * @return true if the transfer is should proceed, false otherwise
     */
    protected boolean acceptTransferBetweenPlayers(int fromPlayerIdx,
        int toPlayerIdx, Card[] cards)
    {
        return true;
    }
    
    /**
     * Processes a request to send a set of cards from one player to another.
     * Calls {@link #acceptTransferBetweenPlayers
     * acceptTransferBetweenPlayers} to determine whether or not
     * to process the transfer, and {@link #transferCardsBetweenPlayers 
     * transferCardsBetweenPlayers} to
     * perform the transfer if accepted.
     *
     * @param client the client object
     * @param playerIndex the index of the player to receive the cards
     * @param cards the cards to send
     * @param cl a listener to notify on success/failure
     */
    public void sendCardsToPlayer (ClientObject client, int playerIndex,
        Card[] cards, ConfirmListener cl)
        throws InvocationException
    {
        int fromPlayerIdx = getPlayerIndex(client);
        
        if (acceptTransferBetweenPlayers(fromPlayerIdx, playerIndex, cards)) {
            transferCardsBetweenPlayers(getPlayerIndex(client), playerIndex,
                cards);
            cl.requestProcessed();
        } else {
            throw new InvocationException("m.transfer_rejected");
        }
    }
    
    /**
     * Sends a set of cards from one player to another.
     *
     * @param fromPlayerIdx the index of the player sending the cards
     * @param toPlayerIdx the index of the player receiving the cards
     * @param cards the cards to be exchanged
     */
    public void transferCardsBetweenPlayers (int fromPlayerIdx,
        int toPlayerIdx, Card[] cards)
    {
        CardGameSender.sendCardsFromPlayer(
            (ClientObject)PresentsServer.omgr.getObject(
                _playerOids[toPlayerIdx]), fromPlayerIdx, cards);
    }
    
    /** The card game object. */
    protected CardGameObject _cardgameobj;
}
