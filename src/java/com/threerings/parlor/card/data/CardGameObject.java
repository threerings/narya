//
// $Id: TurnGameObject.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.parlor.card.data;

import com.threerings.parlor.game.GameObject;

/**
 * Game object class for card games.
 */
public class CardGameObject extends GameObject
{
    /** The field name of the <code>cardGameService</code> field. */
    public static final String CARD_GAME_SERVICE = "cardGameService";

    /** The card game service interface. */
    public CardGameMarshaller cardGameService;

    /**
     * Requests that the <code>cardGameService</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setCardGameService (CardGameMarshaller cardGameService)
    {
        requestAttributeChange(CARD_GAME_SERVICE, cardGameService);
        this.cardGameService = cardGameService;
    }
}
