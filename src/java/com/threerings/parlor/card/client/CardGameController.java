//
// $Id: CardGameController.java,v 1.2 2004/10/15 00:14:23 andrzej Exp $
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

package com.threerings.parlor.card.client;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.card.Log;

import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.Hand;

import com.threerings.parlor.game.GameController;

import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

/**
 * A controller class for card games.  Handles common functions like
 * accepting dealt hands.
 */
public abstract class CardGameController extends GameController
                                         implements MessageListener,
                                                    CardCodes
{
    // Documentation inhertied
    public void init (CrowdContext context, PlaceConfig config)
    {
        super.init(context, config);
        
        _ctx.getClient().getClientObject().addListener(this);
    }
    
    // Documentation inherited
    public void messageReceived (MessageEvent event)
    {   
        if(event.getName().equals(TAKE_HAND))
        {
            handDealt((Hand)event.getArgs()[0]);
        }
    }
    
    /**
     * Called when the server deals the client a new hand of cards.
     *
     * @param hand the hand dealt to the user
     */
    protected void handDealt (Hand hand)
    {}
}
