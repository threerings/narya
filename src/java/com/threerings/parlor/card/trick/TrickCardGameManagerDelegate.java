//
// $Id$
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

package com.threerings.parlor.card.trick;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.GameManager;

import com.threerings.parlor.turn.TurnGameManagerDelegate;

/**
 * A card game manager delegate for trick-based card games, such as
 * Spades and Hearts.
 */
public class TrickCardGameManagerDelegate extends TurnGameManagerDelegate
{   
    /**
     * Constructor.
     *
     * @param manager the game manager
     */
    public TrickCardGameManagerDelegate (TrickCardGameManager manager)
    {
        super(manager);
        
        _tcgmgr = manager;
    }
    
    // Documentation inherited.
    public void setFirstTurnHolder ()
    {
        if (_trickCardGame.getPlayingHand() &&
            _trickCardGame.getPlayingTrick()) {
            super.setFirstTurnHolder();
        }
        else {
            _turnIdx = -1;
        }
    }
    
    // Documentation inherited.
    public void setNextTurnHolder ()
    {
        if (_trickCardGame.getPlayingHand() &&
            _trickCardGame.getPlayingTrick()) {
            super.setNextTurnHolder();
        }
        else {
            _turnIdx = -1;
        }
    }
    
    // Documentation inherited.
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        
        _trickCardGame = (TrickCardGameObject)plobj;
    }
    
    /**
     * Starts a hand of cards.
     */
    public void startHand ()
    {
        _tcgmgr.handWillStart();
        
        _trickCardGame.setPlayingHand(true);
        
        _tcgmgr.handDidStart();
    }
    
    /**
     * Ends a hand of cards.
     */
    public void endHand ()
    {
        _trickCardGame.setPlayingHand(false);
        
        _tcgmgr.handDidEnd();
    }
    
    /**
     * Starts a trick.
     */
    public void startTrick ()
    {
        _tcgmgr.trickWillStart();
        
        _trickCardGame.setPlayingTrick(true);
        
        _tcgmgr.trickDidStart();
    }
    
    /**
     * Ends a trick.
     */
    public void endTrick ()
    {
        _trickCardGame.setPlayingTrick(false);
        
        _tcgmgr.trickDidEnd();
    }
    
    
    /** The trick card game manager. */
    protected TrickCardGameManager _tcgmgr;
    
    /** The game object. */
    protected TrickCardGameObject _trickCardGame;
}
