//
// $Id: CardCodes.java,v 1.2 2004/10/22 23:19:07 andrzej Exp $
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

import com.threerings.presents.data.InvocationCodes;

/**
 * Constants relating to the card services.
 */
public interface CardCodes extends InvocationCodes
{
    /** The suit of hearts. */
    public static final int HEARTS = 0;
    
    /** The suit of diamonds. */
    public static final int DIAMONDS = 1;
    
    /** The suit of clubs. */
    public static final int CLUBS = 2;
    
    /** The suit of spades. */
    public static final int SPADES = 3;
    
    /** The number of the jack. */
    public static final int JACK = 11;
    
    /** The number of the queen. */
    public static final int QUEEN = 12;
    
    /** The number of the king. */
    public static final int KING = 13;
    
    /** The number of the ace. */
    public static final int ACE = 14;
    
    /** The number of the red joker. */
    public static final int RED_JOKER = 15;
    
    /** The number of the black joker. */
    public static final int BLACK_JOKER = 16;
    
    /** A message that carries a Hand of cards to a player. */
    public static final String TAKE_HAND = "take_hand";
}
