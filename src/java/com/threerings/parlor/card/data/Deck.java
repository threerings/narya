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

package com.threerings.parlor.card.data;

import java.util.Collections;

import com.threerings.util.StreamableArrayList;

/**
 * Instances of this class represent decks of cards.
 */
public class Deck extends StreamableArrayList
    implements CardCodes
{
    /**
     * Default constructor creates an unshuffled deck of cards without
     * jokers.
     */
    public Deck ()
    {
        reset(false);
    }
    
    /**
     * Constructor.
     *
     * @param includeJokers whether or not to include the two jokers
     * in the deck
     */
    public Deck (boolean includeJokers)
    {
        reset(includeJokers);
    }
    
    /**
     * Resets the deck to its initial state: an unshuffled deck of
     * 52 or 54 cards, depending on whether the jokers are included.
     *
     * @param includeJokers whether or not to include the two jokers
     * in the deck
     */
    public void reset (boolean includeJokers)
    {
        clear();
        
        for (int i = SPADES; i <= DIAMONDS; i++) {
            for (int j = 2; j <= ACE; j++) {
                add(new Card(j, i));
            }
        }
        
        if (includeJokers) {
            add(new Card(RED_JOKER, 3));
            add(new Card(BLACK_JOKER, 3));
        }
    }
    
    /**
     * Shuffles the deck.
     */
    public void shuffle ()
    {
        Collections.shuffle(this);
    }
    
    /**
     * Deals a hand of cards from the deck.
     *
     * @param size the size of the hand to deal
     * @return the newly created and populated hand, or null
     * if there are not enough cards in the deck to deal the hand
     */
    public Hand dealHand (int size)
    {
        if (size() < size) {
            return null;
            
        } else {
            Hand hand = new Hand();
        
            int cardsLeft = size();
            for (int i = 0; i < size; i++) {
                hand.add(remove(--cardsLeft));
            }
            
            return hand;
        }
    }
    
    /**
     * Returns a hand of cards to the deck.
     *
     * @param hand the hand of cards to return
     */
    public void returnHand (Hand hand)
    {
        addAll(hand);
        hand.clear();
    }
}
