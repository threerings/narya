//
// $Id: Hand.java,v 1.3 2004/10/15 03:09:46 andrzej Exp $
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

import java.util.Iterator;

import com.threerings.io.Streamable;

import com.threerings.util.StreamableArrayList;

/**
 * Instances of this class represent hands of cards.
 */
public class Hand implements CardCodes,
                             Streamable
{
    /** The cards in the hand. */
    public StreamableArrayList cards;
    
    
    /**
     * Default constructor creates an empty hand.
     */
    public Hand ()
    {
        cards = new StreamableArrayList();
    }
    
    /**
     * Counts the members of a particular suit within this hand.
     *
     * @param suit the suit of interest
     * @return the number of cards in the specified suit
     */
    public int getSuitMemberCount (int suit)
    {
        int members = 0;
        
        Iterator it = cards.iterator();
        
        while(it.hasNext()) {
            if(((Card)it.next()).getSuit() == suit) {
                members++;   
            }
        }
        
        return members;
    }
    
    /**
     * Returns a string representation of this hand.
     *
     * @return a description of this hand
     */
    public String toString ()
    {
        return "[cards=" + cards.toString() + "]";
    }
}
