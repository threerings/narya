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

package com.threerings.parlor.card.client;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.FadableImageSprite;
import com.threerings.media.util.Path;

import com.threerings.parlor.card.data.Card;

/**
 * A sprite representing a playing card.
 */
public class CardSprite extends FadableImageSprite
    implements Comparable
{
    /**
     * Creates a new upward-facing card sprite.
     *
     * @param panel the panel responsible for the sprite
     * @param card the card to depict (can be null, in which case the
     * card back will be shown)
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
     * Flip the card from its current displayed card to the specified card.
     */
    public void flip (Card newCard, long duration)
    {
        _flipStamp = 0;
        _flipDuration = duration;
        _flipCard = newCard;

        _scaleFactor = 1.0;
    }

    // Documentation inherited.
    public void tick (long tickStamp)
    {
        super.tick(tickStamp);

        // Take care of any flipping we might be doing.
        if (_flipDuration != -1) {
            if (_flipStamp == 0) {
                _flipStamp = tickStamp;
            }

            long diff = tickStamp - _flipStamp;

            // Set the new scale while we're flipping
            if (diff < _flipDuration/2) {
                _scaleFactor = 1.0 - ((float)diff*2)/_flipDuration;
            } else {
                // Switch the image to the card we're flipping to.
                if (_flipCard != null) {
                    setCard(_flipCard);
                    _flipCard = null;
                }
                _scaleFactor = ((float)diff*2)/_flipDuration - 1.0;
            }

            // If we're done, stop flipping.
            if (_scaleFactor > 1.0) {
                _scaleFactor = 1.0;
                _flipDuration = -1;
            }

            // Make sure we flag our location as needing redrawing
            if (_mgr != null) {
                _mgr.getRegionManager().invalidateRegion(_bounds);
            }
        }

    }

    // Documentation inherited.
    public void paint (Graphics2D gfx)
    {
        if (_scaleFactor <= 0) {
            return;
        }
        // If we are flipping the card, scale it horizontally.
        AffineTransform otrans = gfx.getTransform();
        if (_scaleFactor < 1.0) {
            int xtrans = getX() + getWidth()/2;
            gfx.translate(xtrans, 0);
            gfx.scale(_scaleFactor, 1.0);
            gfx.translate(-xtrans, 0);
        }

        super.paint(gfx);

        gfx.setTransform(otrans);
    }

    /**
     * Compares this to another card sprite based on their cards.
     */
    public int compareTo (Object other)
    {
        CardSprite cs = (CardSprite)other;
        if (_card == null || cs._card == null) {
            return 0;

        } else {
            return _card.compareTo(cs._card);
        }
    }

    /**
     * Updates the mirage according to the current state.
     */
    protected void updateMirage ()
    {
        setMirage((_card != null && _facingUp ) ?
            _panel.getCardImage(_card) : _panel.getCardBackImage());
    }

    /** The panel responsible for the sprite. */
    protected CardPanel _panel;

    /** The depicted card. */
    protected Card _card;

    /** Whether or not the card is facing up. */
    protected boolean _facingUp;

    /** Whether or not the user can drag the card around the board. */
    protected boolean _draggable;

    /** The horizontal scale factor used while flipping the card. */
    protected double _scaleFactor = 1.0;

    /** If flipping, how long the current flip should take (otherwise -1). */
    protected long _flipDuration;

    /** The timestamp for when we started flipping the card. */
    protected long _flipStamp;

    /** The card which will be revealed when we're done flipping. */
    protected Card _flipCard;
}
