//
// $Id: Card.java,v 1.6 2004/10/22 23:19:07 andrzej Exp $
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

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet;

/**
 * Instances of this class represent individual playing cards.
 */
public class Card implements CardCodes,
                             DSet.Entry
{
    
    /**
     * No-arg constructor for deserialization.
     */
    public Card ()
    {}
    
    /**
     * Returns the value of the card, either from 2 to 11 or
     * KING, QUEEN, JACK, ACE, RED_JOKER, or BLACK_JOKER.
     *
     * @return the value of the card
     */
    public int getNumber ()
    {
        return (_value >> 2);
    }
    
    /**
     * Returns the suit of the card: HEARTS, DIAMONDS, CLUBS, or
     * SPADES.  If the card is the joker, the suit is undefined.
     *
     * @return the suit of the card
     */
    public int getSuit ()
    {
        return (_value & 0x03);
    }
    
    /**
     * Checks whether the card is a number card (2 to 10).
     *
     * @return true if the card is a number card, false otherwise
     */
    public boolean isNumber ()
    {
        int number = getNumber();
        
        return number >= 2 && number <= 10;
    }
    
    /**
     * Checks whether the card is a face card (KING, QUEEN, or JACK).
     *
     * @return true if the card is a face card, false otherwise
     */
    public boolean isFace ()
    {
        int number = getNumber();
        
        return number == KING || number == QUEEN || number == JACK;
    }
    
    /**
     * Checks whether the card is an ace.
     *
     * @return true if the card is an ace, false otherwise
     */
    public boolean isAce ()
    {
        return getNumber() == ACE;
    }
    
    /**
     * Checks whether the card is a joker.
     *
     * @return true if the card is a joker, false otherwise
     */
    public boolean isJoker ()
    {
        int number = getNumber();
        
        return number == RED_JOKER || number == BLACK_JOKER;
    }
    
    /**
     * Checks whether or not this card is valid.  The no-arg public constructor
     * for deserialization creates an invalid card.
     *
     * @return true if this card is valid, false if not
     */
    public boolean isValid ()
    {
        int number = getNumber(), suit = getSuit();
        
        return number == RED_JOKER || number == BLACK_JOKER ||
               (number >= 2 && number <= ACE &&
                suit >= HEARTS && suit <= SPADES);
    }
    
    // Documentation inherited.
    public Comparable getKey ()
    {
        if (_key == null) {
            _key = new Byte(_value);
        }
        
        return _key;
    }
    
    /**
     * Returns a hash code for this card.
     *
     * @return this card's hash code
     */
    public int hashCode ()
    {
        return _value;
    }
    
    /**
     * Checks this card for equality with another.
     *
     * @param other the other card to compare
     * @return true if the cards are equal, false otherwise
     */
    public boolean equals (Object other)
    {
        if (other instanceof Card) {
            return _value == ((Card)other)._value;
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns a string representation of this card.
     *
     * @return a description of this card
     */
    public String toString ()
    {
        int number = getNumber();
        
        if (number == RED_JOKER) {
            return "RJ";
        }
        else if (number == BLACK_JOKER) {
            return "BJ";
        }
        else {
            StringBuffer sb = new StringBuffer();
            
            if (number >= 2 && number <= 9) {
                sb.append(Integer.toString(number));
            }
            else {
                switch (number) {
                    case 10: sb.append('T'); break;
                    case JACK: sb.append('J'); break;
                    case QUEEN: sb.append('Q'); break;
                    case KING: sb.append('K'); break;
                    case ACE: sb.append('A'); break;
                    default: sb.append('?'); break;
                }
            }
            
            switch (getSuit()) {
                case HEARTS: sb.append('h'); break;
                case DIAMONDS: sb.append('d'); break;
                case CLUBS: sb.append('c'); break;
                case SPADES: sb.append('s'); break;
                default: sb.append('?'); break;
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Package-only constructor for Deck.
     *
     * @param number the number of the card
     * @param suit the suit of the card
     */
    Card (int number, int suit)
    {
        _value = (byte)((number << 2) | suit);
    }
    
    
    /** The number of the card. */
    protected byte _value;
    
    /** The comparison key. */
    protected transient Byte _key;
}
