//
// $Id: CardGameManager.java,v 1.2 2004/10/13 19:29:12 andrzej Exp $
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

import com.threerings.parlor.card.Log;

import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.Deck;
import com.threerings.parlor.card.data.Hand;

import com.threerings.parlor.game.GameManager;

import com.threerings.presents.dobj.MessageEvent;

/**
 * A manager class for card games.  Handles common functions like dealing hands
 * of cards to all players.
 */
public class CardGameManager extends GameManager
                             implements CardCodes
{
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
    public Hand dealHand(Deck deck, int size, int playerIndex)
    {
        if(deck.cards.size() < size)
        {
            return null;
        }
        else
        {
            Hand hand = deck.dealHand(size);
            
            _omgr.postEvent(
                new MessageEvent(
                    _playerOids[playerIndex],
                    TAKE_HAND, 
                    new Object[] { hand }
                )
            );
            
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
    public Hand[] dealHands(Deck deck, int size)
    {
        if(deck.cards.size() < size * _playerCount)
        {
            return null;
        }
        else
        {
            Hand[] hands = new Hand[_playerCount];
            
            for(int i=0;i<_playerCount;i++)
            {
                hands[i] = dealHand(deck, size, i);   
            }
            
            return hands;
        }
    }    
}
