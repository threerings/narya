//
// $Id: Deck.java,v 1.2 2004/10/15 00:14:23 andrzej Exp $
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

import com.threerings.io.Streamable;

import com.threerings.util.StreamableArrayList;

/**
 * Instances of this class represent decks of cards.
 */
public class Deck implements CardCodes,
                             Streamable
{
    /** The cards in the deck. */
    public StreamableArrayList cards;
    
    
    /**
     * Default constructor creates an unshuffled deck of cards without
     * jokers.
     */
    public Deck ()
    {
        cards = new StreamableArrayList();
        
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
        cards = new StreamableArrayList();
        
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
        cards.clear();
        
        for(int i=HEARTS;i<=SPADES;i++)
        {
            for(int j=2;j<=ACE;j++)
            {
                cards.add(new Card(j, i));
            }
        }
        
        if(includeJokers)
        {
            cards.add(new Card(JOKER, -1));
            cards.add(new Card(JOKER, -1));
        }
    }
    
    /**
     * Shuffles the deck.
     */
    public void shuffle ()
    {
        Collections.shuffle(cards);
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
        if(cards.size() < size)
        {
            return null;
        }
        else
        {
            Hand hand = new Hand();
        
            for(int i=0;i<size;i++)
            {
                hand.cards.add(cards.get(cards.size()-1));
                cards.remove(cards.size()-1);
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
        cards.addAll(hand.cards);
        hand.cards.clear();
    }
    
    /**
     * Returns a string representation of this deck.
     *
     * @return a description of this deck
     */
    public String toString ()
    {
        return "[cards=" + cards.toString() + "]";
    }
}
