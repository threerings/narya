//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.card.trick.server;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.trick.client.TrickCardGameService;
import com.threerings.parlor.card.trick.data.TrickCardGameMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link TrickCardGameProvider}.
 */
public class TrickCardGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public TrickCardGameDispatcher (TrickCardGameProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new TrickCardGameMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case TrickCardGameMarshaller.PLAY_CARD:
            ((TrickCardGameProvider)provider).playCard(
                source,
                (Card)args[0], ((Integer)args[1]).intValue()
            );
            return;

        case TrickCardGameMarshaller.REQUEST_REMATCH:
            ((TrickCardGameProvider)provider).requestRematch(
                source                
            );
            return;

        case TrickCardGameMarshaller.SEND_CARDS_TO_PLAYER:
            ((TrickCardGameProvider)provider).sendCardsToPlayer(
                source,
                ((Integer)args[0]).intValue(), (Card[])args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
