//
// $Id: Card.java,v 1.2 2004/10/13 19:29:12 andrzej Exp $
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
    public Card()
    {}
    
    /**
     * Returns the value of the card, either from 2 to 11 or
     * KING, QUEEN, JACK, ACE, or JOKER.
     *
     * @return the value of the card
     */
    public int getNumber()
    {
        return number;
    }
    
    /**
     * Returns the suit of the card: HEARTS, DIAMONDS, CLUBS, or
     * SPADES.  If the card is the joker, the suit is invalid (-1).
     *
     * @return the suit of the card
     */
    public int getSuit()
    {
        return suit;
    }
    
    /**
     * Checks whether the card is a number card (2 to 10).
     *
     * @return true if the card is a number card, false otherwise
     */
    public boolean isNumber()
    {
        return number >= 2 && number <= 10;
    }
    
    /**
     * Checks whether the card is a face card (KING, QUEEN, or JACK).
     *
     * @return true if the card is a face card, false otherwise
     */
    public boolean isFace()
    {
        return number == KING || number == QUEEN || number == JACK;
    }
    
    /**
     * Checks whether the card is an ace.
     *
     * @return true if the card is an ace, false otherwise
     */
    public boolean isAce()
    {
        return number == ACE;
    }
    
    /**
     * Checks whether the card is a joker.
     *
     * @return true if the card is a joker, false otherwise
     */
    public boolean isJoker()
    {
        return number == JOKER;
    }
    
    /**
     * Checks whether or not this card is valid.  The no-arg public constructor
     * for deserialization creates an invalid card.
     *
     * @return true if this card is valid, false if not
     */
    public boolean isValid()
    {
        return number == JOKER || 
               (number >= 2 && number <= ACE &&
                suit >= HEARTS && suit <= SPADES);
    }
    
    /**
     * Writes this object to the specified output stream.
     *
     * @param oos the output stream
     */
    public void writeObject(ObjectOutputStream oos)
                throws IOException
    {
        oos.defaultWriteObject();
        
        oos.writeInt(number);
        oos.writeInt(suit);
    }
    
    /**
     * Reads this object from the specified input stream.
     *
     * @param ois the input stream
     */
    public void readObject(ObjectInputStream ois)
                throws IOException,
                       ClassNotFoundException
    {
        ois.defaultReadObject();
        
        number = ois.readInt();
        suit = ois.readInt();
        
        key = new Integer((number << 2) | suit);
    }
    
    // documentation inherited
    public Comparable getKey()
    {
        return key;
    }
    
    /**
     * Returns a hash code for this card.
     *
     * @return this card's hash code
     */
    public int hashCode()
    {
        return key.intValue();
    }
    
    /**
     * Checks this card for equality with another.
     *
     * @param other the other card to compare
     * @return true if the cards are equal, false otherwise
     */
    public boolean equals(Object other)
    {
        if(other instanceof Card)
        {
            return number == ((Card)other).number &&
                   suit == ((Card)other).suit;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns a string representation of a given number.
     *
     * @param number the number to represent
     * @return the string representation
     */
    public static String numberToString(int number)
    {
        switch(number)
        {
            case 2: return "Two";
            case 3: return "Three";
            case 4: return "Four";
            case 5: return "Five";
            case 6: return "Six";
            case 7: return "Seven";
            case 8: return "Eight";
            case 9: return "Nine";
            case 10: return "Ten";
            case JACK: return "Jack";
            case QUEEN: return "Queen";
            case KING: return "King";
            case ACE: return "Ace";
            case JOKER: return "Joker";
            default: return "???";
        }
    }
    
    /**
     * Returns a string representation of a particular suit.
     *
     * @param suit the suit to represent
     * @return the string representation
     */
    public static String suitToString(int suit)
    {
        switch(suit)
        {
            case HEARTS: return "Hearts";
            case DIAMONDS: return "Diamonds";
            case CLUBS: return "Clubs";
            case SPADES: return "Spades";
            default: return "???";
        }
    }
    
    /**
     * Returns a string representation of this card.
     *
     * @return a description of this card
     */
    public String toString()
    {
        if(number == JOKER)
        {
            return "Joker";
        }
        else
        {
            return numberToString(number) + " of " + suitToString(suit);
        }
    }
    
    /**
     * Package-only constructor for Deck.
     *
     * @param number the number of the card
     * @param suit the suit of the card
     */
    Card(int number, int suit)
    {
        this.number = number;
        this.suit = suit;
        
        key = new Integer((number << 2) | suit);
    }
    
    
    /** The number of the card. */
    private int number;
    
    /** The suit of the card. */
    private int suit;
    
    /** The comparison key. */
    private Integer key;
}
