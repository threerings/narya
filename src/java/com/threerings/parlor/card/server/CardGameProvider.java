//
// $Id: RoisterService.java 17829 2004-11-12 20:24:43Z mdb $

package com.threerings.parlor.card.server;

import com.threerings.parlor.card.data.Card;

import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Service calls related to card games.
 */
public interface CardGameProvider extends InvocationProvider
{
    /**
     * Sends a set of cards to another player.
     *
     * @param client the client object
     * @param playerIndex the index of the player to receive the cards
     * @param cards the cards to send
     * @param cl a listener to notify on success/failure
     * @exception InvocationException if an error occurs
     */
    public void sendCardsToPlayer (ClientObject client, int playerIndex,
        Card[] cards, ConfirmListener cl)
        throws InvocationException;
}
