//
// $Id: RoisterService.java 17829 2004-11-12 20:24:43Z mdb $

package com.threerings.parlor.card.client;

import com.threerings.parlor.card.data.Card;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Service calls related to card games.
 */
public interface CardGameService extends InvocationService
{
    /**
     * Sends a set of cards to another player.
     *
     * @param client the client object
     * @param playerIndex the index of the player to receive the cards
     * @param cards the cards to send
     * @param cl a listener to notify on success/failure
     */
    public void sendCardsToPlayer (Client client, int playerIndex, Card[] cards,
        ConfirmListener cl);
}
