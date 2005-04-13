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

package com.threerings.parlor.card.trick.data;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;
import com.threerings.parlor.card.data.PlayerCard;
import com.threerings.parlor.turn.data.TurnGameObject;

/**
 * Game objects for trick-based card games must implement this interface.
 */
public interface TrickCardGameObject extends TurnGameObject
{
    /** The state that indicates the game is currently between hands. */
    public static final int BETWEEN_HANDS = 0;
    
    /** The state that indicates the game is currently playing a hand. */
    public static final int PLAYING_HAND = 1;
    
    /** The state that indicates the game is currently playing a trick. */
    public static final int PLAYING_TRICK = 2;
    
    /** The number of states defined for the base trick card game object. */
    public static final int TRICK_STATE_COUNT = 3;
    
    /** Indicates that the player has not requested or accepted a rematch. */
    public static final int NO_REQUEST = 0;
    
    /** Indicates that the player has requested a rematch. */
    public static final int REQUESTS_REMATCH = 1;
    
    /** Indicates that the player has accepted the rematch request. */
    public static final int ACCEPTS_REMATCH = 2;
    
    /**
     * Returns a reference to the trick card game service used to make
     * requests to the server.
     *
     * @return a reference to the trick card game service
     */
    public TrickCardGameMarshaller getTrickCardGameService ();
    
    /**
     * Sets the reference to the trick card game service.
     *
     * @param trickCardGameService the trick card game service
     */
    public void setTrickCardGameService (TrickCardGameMarshaller
        trickCardGameService);
    
    /**
     * Returns the name of the field that contains the trick state: between
     * hands, playing a hand, or playing a trick.
     *
     * @return the name of the trickState field
     */
    public String getTrickStateFieldName ();
    
    /**
     * Returns the trick state: between hands, playing a hand, or playing a
     * trick.
     *
     * @return the trick state
     */
    public int getTrickState ();

    /**
     * Sets the trick state.
     *
     * @param trickState the trick state
     */    
    public void setTrickState (int trickState);
    
    /**
     * Returns the name of the field that contains the history of the trick
     * in terms of the cards played by each player.
     *
     * @return the name of the cardsPlayed field
     */
    public String getCardsPlayedFieldName ();
    
    /**
     * Returns an array containing the history of the trick in terms of the
     * cards played by each player.
     *
     * @return the cards played so far in the trick
     */
    public PlayerCard[] getCardsPlayed ();
    
    /**
     * Sets the array of cards played by each player.
     *
     * @param cardsPlayed the array of cards played
     */
    public void setCardsPlayed (PlayerCard[] cardsPlayed);

    /**
     * Returns the name of the field that contains the history of the last
     * trick in terms of the cards played by each player.
     *
     * @return the name of the lastCardsPlayed field
     */
    public String getLastCardsPlayedFieldName ();
    
    /**
     * Returns an array containing the history of the last trick in terms of
     * the cards played by each player.
     *
     * @return the cards played in the last trick
     */
    public PlayerCard[] getLastCardsPlayed ();
    
    /**
     * Sets the last array of cards played by each player.
     *
     * @param lastCardsPlayed the last array of cards played
     */
    public void setLastCardsPlayed (PlayerCard[] lastCardsPlayed);
    
    /**
     * Returns the name of the field that contains the rematch requests.
     *
     * @return the name of the rematchRequests field
     */
    public String getRematchRequestsFieldName ();
    
    /**
     * Returns the array of rematch requests.
     *
     * @return the array of rematch requests
     */
    public int[] getRematchRequests ();
    
    /**
     * Sets the array of rematch requests.
     *
     * @param rematchRequests the array of rematch requests
     */
    public void setRematchRequests (int[] rematchRequests);
    
    /**
     * Sets an element of the rematch request array.
     *
     * @param rematchRequest the rematch request value
     * @param index the index at which to set the value
     */
    public void setRematchRequestsAt (int rematchRequest, int index);
    
    /**
     * Checks whether a user can play the specified card at this time.
     *
     * @param hand the player's hand
     * @param card the card that the user would like to play
     */
    public boolean isCardPlayable (Hand hand, Card card);
    
    /**
     * Returns the card of the player who took the current trick.
     */
    public PlayerCard getTrickTaker ();
}
