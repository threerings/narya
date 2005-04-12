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

package com.threerings.parlor.card.trick.data;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.trick.client.TrickCardGameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link TrickCardGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TrickCardGameMarshaller extends InvocationMarshaller
    implements TrickCardGameService
{
    /** The method id used to dispatch {@link #playCard} requests. */
    public static final int PLAY_CARD = 1;

    // documentation inherited from interface
    public void playCard (Client arg1, Card arg2)
    {
        sendRequest(arg1, PLAY_CARD, new Object[] {
            arg2
        });
    }

    /** The method id used to dispatch {@link #sendCardsToPlayer} requests. */
    public static final int SEND_CARDS_TO_PLAYER = 2;

    // documentation inherited from interface
    public void sendCardsToPlayer (Client arg1, int arg2, Card[] arg3)
    {
        sendRequest(arg1, SEND_CARDS_TO_PLAYER, new Object[] {
            new Integer(arg2), arg3
        });
    }

}
