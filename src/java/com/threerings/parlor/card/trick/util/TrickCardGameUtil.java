//
// $Id: TrickCardGameObject.java 3382 2005-03-03 19:55:35Z mdb $
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

package com.threerings.parlor.card.trick.util;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;
import com.threerings.parlor.card.data.PlayerCard;
import com.threerings.parlor.card.trick.data.TrickCardCodes;

/**
 * Methods of general utility to trick-taking card games.
 */
public class TrickCardGameUtil
    implements TrickCardCodes
{
    /**
     * For four-player games with fixed partnerships, this returns the index
     * of the player's team.
     *
     * @param pidx the player index
     */
    public static int getTeamIndex (int pidx)
    {
        return pidx & 1;
    }
    
    /**
     * For four-player games with fixed partnerships, this returns the index
     * of the other team.
     *
     * @param tidx the index of the team
     */
    public static int getOtherTeamIndex (int tidx)
    {
        return tidx ^ 1;
    }
    
    /**
     * For four-player games with fixed partnerships, this returns the index
     * of the player's partner.
     */
    public static int getPartnerIndex (int pidx)
    {
        return pidx ^ 2;
    }
    
    /**
     * For four-player games with fixed partnerships, this returns the index
     * of one of the members of a team.
     *
     * @param tidx the index of the team
     * @param midx the index of the player within the team
     */
    public static int getTeamMemberIndex (int tidx, int midx)
    {
        return (midx << 1) | tidx;
    }
    
    /**
     * For four-player games, this returns the index of the player after the
     * specified player going clockwise around the table.
     */
    public static int getNextInClockwiseSequence (int pidx)
    {
        //   2
        // 1   3
        //   0
        return (pidx + 1) & 3;
    }
    
    /**
     * For four-player games with fixed partnerships, this returns the
     * relative location of one player from the point of view of another.
     *
     * @param pidx1 the index of the player to whom the location is relative
     * @param pidx2 the index of the player whose location is desired
     * @return the relative location (TOP, BOTTOM, LEFT, or RIGHT)
     */
    public static int getRelativeLocation (int pidx1, int pidx2)
    {
        return (pidx2 - pidx1) & 3;
    }
    
    /**
     * For four-player games, returns the index of the player to the left of
     * the specified player.
     */
    public static int getLeftIndex (int pidx)
    {
        return (pidx + 1) & 3;
    }
    
    /**
     * For four-player games, returns the index of the player to the right of
     * the specified player.
     */
    public static int getRightIndex (int pidx)
    {
        return (pidx + 3) & 3;
    }
    
    /**
     * For four-player games, returns the index of the player across from the
     * specified player.
     */
    public static int getOppositeIndex (int pidx)
    {
        return pidx ^ 2;
    }
    
    /**
     * Checks whether the player can follow the suit lead with the hand given.
     */
    public static boolean canFollowSuit (PlayerCard[] cardsPlayed, Hand hand)
    {
        return hand.getSuitMemberCount(cardsPlayed[0].card.getSuit()) > 0;
    }
    
    /**
     * Checks whether the specified array contains the given card.
     */
    public static boolean containsCard (PlayerCard[] cards, Card card)
    {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].card.equals(card)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines the number of cards that belong to the specified suit within
     * the array given.
     */
    public static int countSuitMembers (PlayerCard[] cards, int suit)
    {
        int count = 0;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].card.getSuit() == suit) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks whether the proposed card follows the suit lead.
     */
    public static boolean followsSuit (PlayerCard[] cardsPlayed, Card card)
    {
        return cardsPlayed[0].card.getSuit() == card.getSuit();
    }
    
    /**
     * Returns the highest card (according to the standard A,K,...,2 ordering)
     * in the suit lead, with an optional trump suit.
     *
     * @param trumpSuit the trump suit, or -1 for none
     */
    public static PlayerCard getHighestInLeadSuit (PlayerCard[] cardsPlayed,
        int trumpSuit)
    {
        PlayerCard highest = cardsPlayed[0];
        for (int i = 1; i < cardsPlayed.length; i++) {
            PlayerCard other = cardsPlayed[i];
            if ((other.card.getSuit() == highest.card.getSuit() &&
                    other.card.compareTo(highest.card) > 0) ||
                (other.card.getSuit() == trumpSuit &&
                    highest.card.getSuit() != trumpSuit)) {
                highest = other;
            }
        }
        return highest;
    }
}
