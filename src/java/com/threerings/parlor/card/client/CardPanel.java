//
// $Id: CardPanel.java,v 1.3 2004/10/15 03:09:46 andrzej Exp $
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
        public CardSpriteClickedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            ((CardSpriteObserver)observer).cardSpriteClicked(_sprite, _me);
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteDragged. */
    protected static class CardSpriteDraggedOp implements ObserverList.ObserverOp
    {
        public CardSpriteDraggedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            ((CardSpriteObserver)observer).cardSpriteDragged(_sprite, _me);
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    
    /**
     * Constructor.
     *
     * @param frameManager the frame manager
     */
    public CardPanel (FrameManager frameManager)
    {
        super(frameManager);
        
        MouseAdapter ma = new MouseAdapter() {
            public void mousePressed (MouseEvent me) {
                ArrayList al = new ArrayList();
                    
                _spritemgr.getHitSprites(al, me.getX(), me.getY());
                    
                if(al.size() > 0) {
                    Iterator it = al.iterator();
                    int highestLayer = Integer.MIN_VALUE;
                    CardSprite highestSprite = null;
                        
                    while(it.hasNext()) {
                        Sprite sprite = (Sprite)it.next();
                            
                        if(sprite instanceof CardSprite) {
                            CardSprite cs = (CardSprite)sprite;
                                
                            if(cs.getRenderOrder() > highestLayer) {
                                highestLayer = cs.getRenderOrder();
                                highestSprite = cs;
                            }
                        }
                    }
                        
                    _activeCardSprite = highestSprite;
                       
                    if(_activeCardSprite != null) {
                        _handleX = _activeCardSprite.getX() - me.getX();
                        _handleY = _activeCardSprite.getY() - me.getY();
                            
                        _hasBeenDragged = false;
                    }
                }
                else {
                    _activeCardSprite = null;
                }
            }  
            public void mouseReleased (MouseEvent me) {
                if(_activeCardSprite != null && _hasBeenDragged) {
                    _activeCardSprite.queueNotification(
                        new CardSpriteDraggedOp(_activeCardSprite, me)
                    );
                }
            }
            public void mouseClicked (MouseEvent me) {
                if(_activeCardSprite != null) {
                    _activeCardSprite.queueNotification(
                        new CardSpriteClickedOp(_activeCardSprite, me)
                    );
                }
            }
        };
            
        addMouseListener(ma);
        
        MouseMotionAdapter mma = new MouseMotionAdapter() {
            public void mouseDragged (MouseEvent me)
            {
                if(_activeCardSprite != null &&
                   _activeCardSprite.isDraggable()) {
                    _activeCardSprite.setLocation(
                        me.getX() + _handleX,
                        me.getY() + _handleY
                    );
                        
                    _hasBeenDragged = true;
                }
            }
        };
        
        addMouseMotionListener(mma);
    }
    
    /**
     * Returns the image for the back of a playing card.
     *
     * @return the card back image
     */
    public abstract Mirage getCardBackImage ();
    
    /**
     * Returns the image for the front of the specified card.
     *
     * @param card the desired card
     * @return the card front image
     */
    public abstract Mirage getCardImage (Card card);
    
    
    /** The last card sprite pressed. */
    protected CardSprite _activeCardSprite;
    
    /** The location of the cursor in the active sprite. */
    protected int _handleX, _handleY;
    
    /** Whether or not the active sprite has been dragged. */
    protected boolean _hasBeenDragged;
}
