//
// $Id$

package com.threerings.parlor.card.data;

import com.threerings.parlor.card.client.CardGameService;
import com.threerings.parlor.card.data.Card;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link CardGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class CardGameMarshaller extends InvocationMarshaller
    implements CardGameService
{
    /** The method id used to dispatch {@link #sendCardsToPlayer} requests. */
    public static final int SEND_CARDS_TO_PLAYER = 1;

    // documentation inherited from interface
    public void sendCardsToPlayer (Client arg1, int arg2, Card[] arg3, ConfirmListener arg4)
    {
        ConfirmMarshaller listener4 = new ConfirmMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, SEND_CARDS_TO_PLAYER, new Object[] {
            new Integer(arg2), arg3, listener4
        });
    }

}
