//
// $Id: CardSprite.java,v 1.2 2004/10/15 00:14:23 andrzej Exp $
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

import com.threerings.media.image.Mirage;

import com.threerings.media.sprite.OrientableImageSprite;

import com.threerings.parlor.card.data.Card;

/**
 * A sprite representing a playing card.
 */
public class CardSprite extends OrientableImageSprite
{
    /**
     * Creates a new upward-facing card sprite.
     *
     * @param panel the panel responsible for the sprite
     * @param card the card to depict
     */
    public CardSprite (CardPanel panel, Card card)
    {
        _panel = panel;
        _card = card;
        _facingUp = true;
        
        updateMirage();
    }
    
    /**
     * Creates a new card sprite.
     *
     * @param panel the panel responsible for the sprite
     * @param card the card to depict
     * @param facingUp whether or not the card should be facing up
     */
    public CardSprite (CardPanel panel, Card card, boolean facingUp)
    {
        _panel = panel;
        _card = card;
        _facingUp = facingUp;
        
        updateMirage();
    }
    
    /**
     * Sets the card to depict.
     *
     * @param card the new card
     */
    public void setCard (Card card)
    {
        _card = card;
        
        updateMirage();
    }
    
    /**
     * Returns the card being depicted.
     *
     * @return the current card
     */
    public Card getCard ()
    {
        return _card; 
    }
    
    /**
     * Turns this card up or down.
     *
     * @param facingUp whether or not the card should be facing up
     */
    public void setFacingUp (boolean facingUp)
    {
        _facingUp = facingUp;
        
        updateMirage();
    }
    
    /**
     * Checks whether this card is facing up or down.
     *
     * @return true if the card is facing up, false if facing down
     */
    public boolean isFacingUp ()
    {
        return _facingUp;
    }
    
    /**
     * Sets whether or not the user can drag this card around the board.
     *
     * @param draggable whether or not the user can drag the card
     */
    public void setDraggable (boolean draggable)
    {
        _draggable = draggable;
    }
    
    /**
     * Checks whether or not the user can drag this card.
     *
     * @return true if the user can drag the card, false if not
     */
    public boolean isDraggable ()
    {
        return _draggable;
    }
    
    /**
     * Updates the mirage according to the current state.
     */
    private void updateMirage ()
    {
        setMirage(_facingUp ? _panel.getCardImage(_card) : _panel.getCardBackImage());
    }
    
    
    /** The panel responsible for the sprite. */
    protected CardPanel _panel;
    
    /** The depicted card. */
    protected Card _card;
    
    /** Whether or not the card is facing up. */
    protected boolean _facingUp;
    
    /** Whether or not the user can drag the card around the board. */
    protected boolean _draggable;
}
