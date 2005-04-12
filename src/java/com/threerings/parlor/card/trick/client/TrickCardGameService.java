//
// $Id: RoisterService.java 17829 2004-11-12 20:24:43Z mdb $

package com.threerings.parlor.card.trick.client;

import com.threerings.parlor.card.data.Card;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Service calls related to trick card games.
 */
public interface TrickCardGameService extends InvocationService
{
    /**
     * Sends a group of cards to the player at the specified index.
     *
     * @param client the client object
     * @param toidx the index of the player to send the cards to
     * @param cards the cards to send
     */
    public void sendCardsToPlayer (Client client, int toidx, Card[] cards);
    
    /**
     * Plays a card in the trick.
     *
     * @param client the client object
     * @param card the card to play
     */
    public void playCard (Client client, Card card);
}
