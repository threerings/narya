//
// $Id: CardPanel.java,v 1.1 2004/10/13 02:03:26 andrzej Exp $
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

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import com.samskivert.util.ObserverList;

import com.threerings.media.FrameManager;
import com.threerings.media.VirtualMediaPanel;

import com.threerings.media.image.Mirage;

import com.threerings.media.sprite.Sprite;

import com.threerings.parlor.card.Log;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.Deck;
import com.threerings.parlor.card.data.Hand;

/**
 * Extends VirtualMediaPanel to provide services specific to rendering
 * and manipulating playing cards.
 */
public abstract class CardPanel extends VirtualMediaPanel
                                implements CardCodes
{
    /** Calls CardSpriteObserver.cardSpriteClicked. */ 
    protected static class CardSpriteClickedOp implements ObserverList.ObserverOp
    {
        protected CardSprite sprite;
        protected MouseEvent me;
        
        public CardSpriteClickedOp(CardSprite sprite, MouseEvent me)
        {
            this.sprite = sprite;
            this.me = me;
        }
        
        public boolean apply(Object observer)
        {
            ((CardSpriteObserver)observer).cardSpriteClicked(sprite, me);
            return true;
        }
    }
    
    /** Calls CardSpriteObserver.cardSpriteDragged. */
    protected static class CardSpriteDraggedOp implements ObserverList.ObserverOp
    {
        protected CardSprite sprite;
        protected MouseEvent me;
        
        public CardSpriteDraggedOp(CardSprite sprite, MouseEvent me)
        {
            this.sprite = sprite;
            this.me = me;
        }
        
        public boolean apply(Object observer)
        {
            ((CardSpriteObserver)observer).cardSpriteDragged(sprite, me);
            return true;
        }
    }
    
    
    /**
     * Constructor.
     *
     * @param frameManager the frame manager
     */
    public CardPanel(FrameManager frameManager)
    {
        super(frameManager);
        
        addMouseListener(
            new MouseAdapter()
            {
                public void mousePressed(MouseEvent me)
                {
                    ArrayList al = new ArrayList();
                    
                    _spritemgr.getHitSprites(al, me.getX(), me.getY());
                    
                    if(al.size() > 0)
                    {
                        Iterator it = al.iterator();
                        int highestLayer = Integer.MIN_VALUE;
                        CardSprite highestSprite = null;
                        
                        while(it.hasNext())
                        {
                            Sprite sprite = (Sprite)it.next();
                            
                            if(sprite instanceof CardSprite)
                            {
                                CardSprite cs = (CardSprite)sprite;
                                
                                if(cs.getRenderOrder() > highestLayer)
                                {
                                    highestLayer = cs.getRenderOrder();
                                    highestSprite = cs;
                                }
                            }
                        }
                        
                        activeCardSprite = highestSprite;
                        
                        if(activeCardSprite != null)
                        {
                            handleX = activeCardSprite.getX() - me.getX();
                            handleY = activeCardSprite.getY() - me.getY();
                            
                            hasBeenDragged = false;
                        }
                    }
                    else
                    {
                        activeCardSprite = null;
                    }
                }
                
                public void mouseReleased(MouseEvent me)
                {
                    if(activeCardSprite != null && hasBeenDragged)
                    {
                        activeCardSprite.queueNotification(
                            new CardSpriteDraggedOp(activeCardSprite, me)
                        );
                    }
                }
                
                public void mouseClicked(MouseEvent me)
                {
                    if(activeCardSprite != null)
                    {
                        activeCardSprite.queueNotification(
                            new CardSpriteClickedOp(activeCardSprite, me)
                        );
                    }
                }
            }
        );
        
        addMouseMotionListener(
            new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent me)
                {
                    if(activeCardSprite != null &&
                       activeCardSprite.isDraggable())
                    {
                        activeCardSprite.setLocation(
                            me.getX() + handleX,
                            me.getY() + handleY
                        );
                        
                        hasBeenDragged = true;
                    }
                }
            }
        );
    }
    
    /**
     * Returns the image for the back of a playing card.
     *
     * @return the card back image
     */
    public abstract Mirage getCardBackImage();
    
    /**
     * Returns the image for the front of the specified card.
     *
     * @param card the desired card
     * @return the card front image
     */
    public abstract Mirage getCardImage(Card card);
    
    
    /** The last card sprite pressed. */
    private CardSprite activeCardSprite;
    
    /** The location of the cursor in the active sprite. */
    private int handleX, handleY;
    
    /** Whether or not the active sprite has been dragged. */
    private boolean hasBeenDragged;
}
