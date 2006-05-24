//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link TrickCardGameService}.
 */
public interface TrickCardGameProvider extends InvocationProvider
{
    /**
     * Handles a {@link TrickCardGameService#playCard} request.
     */
    public void playCard (ClientObject caller, Card arg1, int arg2);

    /**
     * Handles a {@link TrickCardGameService#requestRematch} request.
     */
    public void requestRematch (ClientObject caller);

    /**
     * Handles a {@link TrickCardGameService#sendCardsToPlayer} request.
     */
    public void sendCardsToPlayer (ClientObject caller, int arg1, Card[] arg2);
}
