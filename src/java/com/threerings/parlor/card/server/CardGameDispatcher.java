//
// $Id$

package com.threerings.parlor.card.server;

import com.threerings.parlor.card.client.CardGameService;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardGameMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link CardGameProvider}.
 */
public class CardGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public CardGameDispatcher (CardGameProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new CardGameMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case CardGameMarshaller.SEND_CARDS_TO_PLAYER:
            ((CardGameProvider)provider).sendCardsToPlayer(
                source,
                ((Integer)args[0]).intValue(), (Card[])args[1], (ConfirmListener)args[2]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
