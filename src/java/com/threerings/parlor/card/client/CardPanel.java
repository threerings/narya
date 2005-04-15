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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.MouseInputAdapter;

import com.samskivert.util.ObserverList;
import com.samskivert.util.QuickSort;

import com.threerings.media.FrameManager;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;
import com.threerings.media.util.PathSequence;
import com.threerings.media.util.Pathable;

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
    /** The selection mode in which cards are not selectable. */
    public static final int NONE = 0;
    
    /** The selection mode in which the user can play a single card. */
    public static final int PLAY_SINGLE = 1;
    
    /** The selection mode in which the user can select a single card. */
    public static final int SELECT_SINGLE = 2;
    
    /** The selection mode in which the user can select multiple cards. */
    public static final int SELECT_MULTIPLE = 3;
    
    /**
     * A predicate class for {@link CardSprite}s.  Provides control over which
     * cards are selectable, playable, etc.
     */
    public static interface CardSpritePredicate
    {
        /**
         * Evaluates the specified sprite.
         */
        public boolean evaluate (CardSprite sprite);
    }
    
    /**
     * A listener for card selection/deselection.
     */
    public static interface CardSelectionObserver
    {
        /**
         * Called when a card has been played.
         */
        public void cardSpritePlayed (CardSprite sprite);
        
        /**
         * Called when a card has been selected.
         */
        public void cardSpriteSelected (CardSprite sprite);
        
        /**
         * Called when a card has been deselected.
         */
        public void cardSpriteDeselected (CardSprite sprite);
    }
    
    /**
     * Constructor.
     *
     * @param frameManager the frame manager
     */
    public CardPanel (FrameManager frameManager)
    {
        super(frameManager);
        
        // add a listener for mouse events
        CardListener cl = new CardListener();
        addMouseListener(cl);
        addMouseMotionListener(cl);
    }
    
    /**
     * Returns the full-sized image for the back of a playing card.
     */
    public abstract Mirage getCardBackImage ();
    
    /**
     * Returns the full-sized image for the front of the specified card.
     */
    public abstract Mirage getCardImage (Card card);
    
    /**
     * Returns the small-sized image for the back of a playing card.
     */
    public abstract Mirage getMicroCardBackImage ();
    
    /**
     * Returns the small-sized image for the front of the specified card.
     */
    public abstract Mirage getMicroCardImage (Card card);
    
    /**
     * Sets the location of the hand (the location of the center of the hand's
     * upper edge).
     */
    public void setHandLocation (int x, int y)
    {
        _handLocation.setLocation(x, y);
    }
    
    /**
     * Sets the horizontal spacing between cards in the hand.
     */
    public void setHandSpacing (int spacing)
    {
        _handSpacing = spacing;
    }
    
    /**
     * Sets the vertical distance to offset cards that are selectable or
     * playable.
     */
    public void setSelectableCardOffset (int offset)
    {
        _selectableCardOffset = offset;
    }
    
    /**
     * Sets the vertical distance to offset cards that are selected.
     */
    public void setSelectedCardOffset (int offset)
    {
        _selectedCardOffset = offset;
    }
    
    /**
     * Sets the selection mode for the hand (NONE, PLAY_SINGLE, SELECT_SINGLE,
     * or SELECT_MULTIPLE).  Changing the selection mode does not change the
     * current selection.
     */
    public void setHandSelectionMode (int mode)
    {
        _handSelectionMode = mode;
        
        // update the offsets of all cards in the hand
        updateHandOffsets();
    }
    
    /**
     * Sets the selection predicate that determines which cards from the hand
     * may be selected (if null, all cards may be selected).  Changing the
     * predicate does not change the current selection.
     */
    public void setHandSelectionPredicate (CardSpritePredicate pred)
    {
        _handSelectionPredicate = pred;
        
        // update the offsets of all cards in the hand
        updateHandOffsets();
    }
    
    /**
     * Returns the currently selected hand sprite (null if no sprites are
     * selected, the first sprite if multiple sprites are selected).
     */
    public CardSprite getSelectedHandSprite ()
    {
        return _selectedHandSprites.size() == 0 ?
            null : (CardSprite)_selectedHandSprites.get(0);
    }
    
    /**
     * Returns an array containing the currently selected hand sprites
     * (returns an empty array if no sprites are selected).
     */
    public CardSprite[] getSelectedHandSprites ()
    {
        return (CardSprite[])_selectedHandSprites.toArray(
            new CardSprite[_selectedHandSprites.size()]);
    }
    
    /**
     * Programmatically plays a sprite in the hand.
     */
    public void playHandSprite (final CardSprite sprite)
    {
        // notify the observers
        ObserverList.ObserverOp op = new ObserverList.ObserverOp() {
            public boolean apply (Object obs) {
                ((CardSelectionObserver)obs).cardSpritePlayed(sprite);
                return true;
            }
        };
        _handSelectionObservers.apply(op);
    }
    
    /**
     * Programmatically selects a sprite in the hand.
     */
    public void selectHandSprite (final CardSprite sprite)
    {
        // make sure it's not already selected
        if (_selectedHandSprites.contains(sprite)) {
            return;
        }
        
        // if in single card mode and there's another card selected,
        // deselect it
        if (_handSelectionMode == SELECT_SINGLE) {
            CardSprite oldSprite = getSelectedHandSprite();
            if (oldSprite != null) {
                deselectHandSprite(oldSprite);
            }
        }
        
        // add to list and update offset
        _selectedHandSprites.add(sprite);
        sprite.setLocation(sprite.getX(), getHandY(sprite));
        
        // notify the observers
        ObserverList.ObserverOp op = new ObserverList.ObserverOp() {
            public boolean apply (Object obs) {
                ((CardSelectionObserver)obs).cardSpriteSelected(sprite);
                return true;
            }
        };
        _handSelectionObservers.apply(op);
    }
    
    /**
     * Programmatically deselects a sprite in the hand.
     */
    public void deselectHandSprite (final CardSprite sprite)
    {
        // make sure it's selected
        if (!_selectedHandSprites.contains(sprite)) {
            return;
        }
        
        // remove from list and update offset
        _selectedHandSprites.remove(sprite);
        sprite.setLocation(sprite.getX(), getHandY(sprite));
        
        // notify the observers
        ObserverList.ObserverOp op = new ObserverList.ObserverOp() {
            public boolean apply (Object obs) {
                ((CardSelectionObserver)obs).cardSpriteDeselected(sprite);
                return true;
            }
        };
        _handSelectionObservers.apply(op);
    }
    
    /**
     * Clears any existing hand sprite selection.
     */
    public void clearHandSelection ()
    {
        CardSprite[] sprites = getSelectedHandSprites();
        for (int i = 0; i < sprites.length; i++) {
            deselectHandSprite(sprites[i]);
        }
    }
    
    /**
     * Adds an object to the list of observers to notify when cards in the
     * hand are selected/deselected.
     */
    public void addHandSelectionObserver (CardSelectionObserver obs)
    {
        _handSelectionObservers.add(obs);
    }
    
    /**
     * Removes an object from the hand selection observer list.
     */
    public void removeHandSelectionObserver (CardSelectionObserver obs)
    {
        _handSelectionObservers.remove(obs);
    }
    
    /**
     * Fades a hand of cards in.
     *
     * @param hand the hand of cards
     * @param fadeDuration the amount of time to spend fading in
     * the entire hand
     */
    public void setHand (Hand hand, long fadeDuration)
    {
        // make sure no cards are hanging around
        clearHand();
        
        // create the sprites
        int size = hand.size();
        for (int i = 0; i < size; i++) {
            CardSprite cs = new CardSprite(this, (Card)hand.get(i));
            _handSprites.add(cs);
        }
        
        // sort them
        QuickSort.sort(_handSprites);
        
        // fade them in at proper locations and layers
        long cardDuration = fadeDuration / size;
        for (int i = 0; i < size; i++) {
            CardSprite cs = (CardSprite)_handSprites.get(i);
            cs.setLocation(getHandX(size, i), _handLocation.y);
            cs.setRenderOrder(i);
            cs.addSpriteObserver(_handSpriteObserver);
            addSprite(cs);
            cs.fadeIn(i * cardDuration, cardDuration);
        }
    }
    
    /**
     * Fades a hand of cards in face-down.
     *
     * @param size the size of the hand
     * @param fadeDuration the amount of time to spend fading in
     * each card
     */
    public void setHand (int size, long fadeDuration)
    {
        // fill hand will null entries to signify unknown cards
        Hand hand = new Hand();
        for (int i = 0; i < size; i++) {
            hand.add(null);
        }
        setHand(hand, fadeDuration);
    }
    
    /**
     * Shows a hand that was previous set face-down.
     *
     * @param hand the hand of cards
     */
    public void showHand (Hand hand)
    {
        // sort the hand
        QuickSort.sort(hand);
        
        // set the sprites
        int len = Math.min(_handSprites.size(), hand.size());
        for (int i = 0; i < len; i++) {
            CardSprite cs = (CardSprite)_handSprites.get(i);
            cs.setCard((Card)hand.get(i));
        }
    }
    
    /**
     * Returns the first sprite in the hand that corresponds to the
     * specified card, or null if the card is not in the hand.
     */
    public CardSprite getHandSprite (Card card)
    {
        return getCardSprite(_handSprites, card);
    }
    
    /**
     * Clears all cards from the hand.
     */
    public void clearHand ()
    {
        clearHandSelection();
        clearSprites(_handSprites);
    }
    
    /**
     * Clears all cards from the board.
     */
    public void clearBoard ()
    {
        clearSprites(_boardSprites);
    }
    
    /**
     * Flies a set of cards from the hand into the ether.  Clears any selected
     * cards.
     *
     * @param cards the card sprites to remove from the hand
     * @param dest the point to fly the cards to
     * @param flightDuration the duration of the cards' flight
     * @param fadePortion the amount of time to spend fading out
     * as a proportion of the flight duration
     */
    public void flyFromHand (CardSprite[] cards, Point dest,
        long flightDuration, float fadePortion)
    {
        // fly each sprite over, removing it from the hand immediately and
        // from the board when it finishes its path
        for (int i = 0; i < cards.length; i++) {
            removeFromHand(cards[i]);
            LinePath flight = new LinePath(dest, flightDuration);
            cards[i].addSpriteObserver(_pathEndRemover);
            cards[i].moveAndFadeOut(flight, flightDuration, fadePortion);
        }
        
        // adjust the hand to cover the hole
        adjustHand(flightDuration, false);
    }
    
    /**
     * Flies a set of cards from the ether into the hand.  Clears any selected
     * cards.  If the drop duration is nonzero, the cards will first fly to
     * the selected card offset and then drop slowly into the hand.
     *
     * @param cards the cards to add to the hand
     * @param src the point to fly the cards from
     * @param flightDuration the duration of the cards' flight
     * @param dropDuration the duration of the cards' drop into the
     * hand
     * @param fadePortion the amount of time to spend fading in
     * as a proportion of the flight duration
     */
    public void flyIntoHand (Card[] cards, Point src, long flightDuration,
        long dropDuration, float fadePortion)
    {
        // first create the sprites and add them to the list
        CardSprite[] sprites = new CardSprite[cards.length];
        for (int i = 0; i < cards.length; i++) {
            sprites[i] = new CardSprite(this, cards[i]);
            _handSprites.add(sprites[i]);
        }
        
        // settle the hand
        adjustHand(flightDuration, true);
        
        // then set the layers and fly the cards in
        int size = _handSprites.size();
        for (int i = 0; i < sprites.length; i++) {
            int idx = _handSprites.indexOf(sprites[i]);
            sprites[i].setLocation(src.x, src.y);
            sprites[i].setRenderOrder(idx);
            sprites[i].addSpriteObserver(_handSpriteObserver);
            addSprite(sprites[i]);
            
            // if dropping, create a path sequence combining flight and drop
            Path path;
            long pathDuration;
            Point hp = new Point(getHandX(size, idx), _handLocation.y);
            if (dropDuration > 0) {
                LinePath flight = new LinePath(new Point(hp.x, hp.y -
                        _selectedCardOffset), flightDuration),
                    drop = new LinePath(hp, dropDuration);
                path = new PathSequence(flight, drop);
                pathDuration = flightDuration + dropDuration;
                
            } else {
                path = new LinePath(hp, flightDuration);
                pathDuration = flightDuration;
            }
            sprites[i].moveAndFadeIn(path, pathDuration, fadePortion);
        }
    }
    
    /**
     * Flies a set of cards from the ether into the ether.
     *
     * @param cards the cards to fly across
     * @param src the point to fly the cards from
     * @param dest the point to fly the cards to
     * @param flightDuration the duration of the cards' flight
     * @param cardDelay the amount of time to wait between cards
     * @param fadePortion the amount of time to spend fading in and out
     * as a proportion of the flight duration
     */
    public void flyAcross (Card[] cards, Point src, Point dest,
        long flightDuration, long cardDelay, float fadePortion)
    {
        for (int i = 0; i < cards.length; i++) {
            // add on top of all board sprites
            CardSprite cs = new CardSprite(this, cards[i]);
            cs.setRenderOrder(getHighestBoardLayer() + 1 + i);
            cs.setLocation(src.x, src.y);
            addSprite(cs);
            
            // prepend an initial delay to all cards after the first
            Path path;
            long pathDuration;
            LinePath flight = new LinePath(dest, flightDuration);
            if (i > 0) {
                long delayDuration = cardDelay * i;
                LinePath delay = new LinePath(src, delayDuration);
                path = new PathSequence(delay, flight);
                pathDuration = delayDuration + flightDuration;
                
            } else {
                path = flight;
                pathDuration = flightDuration;
            }
            cs.addSpriteObserver(_pathEndRemover);
            cs.moveAndFadeInAndOut(path, pathDuration, fadePortion);
        }
    }
    
    /**
     * Flies a set of cards from the ether into the ether face-down.
     *
     * @param number the number of cards to fly across
     * @param src the point to fly the cards from
     * @param dest the point to fly the cards to
     * @param flightDuration the duration of the cards' flight
     * @param cardDelay the amount of time to wait between cards
     * @param fadePortion the amount of time to spend fading in and out
     * as a proportion of the flight duration
     */
    public void flyAcross (int number, Point src, Point dest,
        long flightDuration, long cardDelay, float fadePortion)
    {
        // use null values to signify unknown cards
        flyAcross(new Card[number], src, dest, flightDuration,
            cardDelay, fadePortion);
    }
    
    /**
     * Flies a card from the hand onto the board.  Clears any cards selected.
     *
     * @param card the sprite to remove from the hand
     * @param dest the point to fly the card to
     * @param flightDuration the duration of the card's flight
     */
    public void flyFromHandToBoard (CardSprite card, Point dest,
        long flightDuration)
    {
        // fly it over
        LinePath flight = new LinePath(dest, flightDuration);
        card.move(flight);
        
        // lower the board so that the card from hand is on top
        lowerBoardSprites(card.getRenderOrder() - 1);
        
        // move from one list to the other
        removeFromHand(card);
        _boardSprites.add(card);
        
        // adjust the hand to cover the hole
        adjustHand(flightDuration, false);
    }
    
    /**
     * Flies a card from the ether onto the board.
     *
     * @param card the card to add to the board
     * @param src the point to fly the card from
     * @param dest the point to fly the card to
     * @param flightDuration the duration of the card's flight
     * @param fadePortion the amount of time to spend fading in as a
     * proportion of the flight duration
     */
    public void flyToBoard (Card card, Point src, Point dest,
        long flightDuration, float fadePortion)
    {
        // add it on top of the existing cards
        CardSprite cs = new CardSprite(this, card);
        cs.setRenderOrder(getHighestBoardLayer() + 1);
        cs.setLocation(src.x, src.y);
        addSprite(cs);
        _boardSprites.add(cs);
        
        // and fly it over
        LinePath flight = new LinePath(dest, flightDuration);
        cs.moveAndFadeIn(flight, flightDuration, fadePortion);
    }
    
    /**
     * Adds a card to the board immediately.
     *
     * @param card the card to add to the board
     * @param dest the point at which to add the card
     */
    public void addToBoard (Card card, Point dest)
    {
        CardSprite cs = new CardSprite(this, card);
        cs.setRenderOrder(getHighestBoardLayer() + 1);
        cs.setLocation(dest.x, dest.y);
        addSprite(cs);
        _boardSprites.add(cs);
    }
    
    /**
     * Flies a set of cards from the board into the ether.
     *
     * @param cards the cards to remove from the board
     * @param dest the point to fly the cards to
     * @param flightDuration the duration of the cards' flight
     * @param fadePortion the amount of time to spend fading out as a
     * proportion of the flight duration
     */
    public void flyFromBoard (CardSprite[] cards, Point dest,
        long flightDuration, float fadePortion)
    {
        for (int i = 0; i < cards.length; i++) {
            LinePath flight = new LinePath(dest, flightDuration);
            cards[i].addSpriteObserver(_pathEndRemover);
            cards[i].moveAndFadeOut(flight, flightDuration, fadePortion);
            _boardSprites.remove(cards[i]);
        }
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        gfx.setColor(DEFAULT_BACKGROUND);
        gfx.fill(dirtyRect);
        super.paintBehind(gfx, dirtyRect);
    }

    /**
     * Flies a set of cards from the board into the ether through an
     * intermediate point.
     *
     * @param cards the cards to remove from the board
     * @param dest1 the first point to fly the cards to
     * @param dest2 the final destination of the cards
     * @param flightDuration the duration of the cards' flight
     * @param fadePortion the amount of time to spend fading out as a
     * proportion of the flight duration
     */
    public void flyFromBoard (CardSprite[] cards, Point dest1, Point dest2,
        long flightDuration, float fadePortion)
    {
        for (int i = 0; i < cards.length; i++) {
            PathSequence flight = new PathSequence(
                new LinePath(dest1, flightDuration/2),
                new LinePath(dest1, dest2, flightDuration/2));
            cards[i].addSpriteObserver(_pathEndRemover);
            cards[i].moveAndFadeOut(flight, flightDuration, fadePortion);
            _boardSprites.remove(cards[i]);
        }
    }
    
    /**
     * Returns the first card sprite in the specified list that represents
     * the specified card, or null if there is no such sprite in the list.
     */
    protected CardSprite getCardSprite (ArrayList list, Card card)
    {
        for (int i = 0; i < list.size(); i++) {
            CardSprite cs = (CardSprite)list.get(i);
            if (card.equals(cs.getCard())) {
                return cs;
            }
        }
        return null;
    }
    
    /**
     * Expands or collapses the hand to accommodate new cards or cover the
     * space left by removed cards.  Skips unmanaged sprites.  Clears out
     * any selected cards.
     *
     * @param adjustDuration the amount of time to spend settling the cards
     * into their new locations
     * @param updateLayers whether or not to update the layers of the cards
     */
    protected void adjustHand (long adjustDuration, boolean updateLayers)
    {
        // clear out selected cards
        clearHandSelection();
        
        // Sort the hand
        QuickSort.sort(_handSprites);
        
        // Move each card to its proper position (and, optionally, layer)
        int size = _handSprites.size();
        for (int i = 0; i < size; i++) {
            CardSprite cs = (CardSprite)_handSprites.get(i);
            if (!isManaged(cs)) {
                continue;
            }
            if (updateLayers) {
                removeSprite(cs);
                cs.setRenderOrder(i);
                addSprite(cs);
            }
            LinePath adjust = new LinePath(new Point(getHandX(size, i),
                _handLocation.y), adjustDuration);
            cs.move(adjust);
        }
    }
    
    /**
     * Removes a card from the hand, deselecting it if selected.
     */
    protected void removeFromHand (CardSprite card)
    {
        if (_selectedHandSprites.contains(card)) {
            deselectHandSprite(card);
        }
        _handSprites.remove(card);
    }
    
    /**
     * Updates the offsets of all the cards in the hand.  If there is only
     * one selectable card, that card will always be raised slightly.
     */
    protected void updateHandOffsets ()
    {
        int size = _handSprites.size();
        for (int i = 0; i < size; i++) {
            CardSprite cs = (CardSprite)_handSprites.get(i);
            cs.setLocation(cs.getX(), getHandY(cs));
        }
    }
    
    /**
     * Given the location and spacing of the hand, returns the x location of
     * the card at the specified index within a hand of the specified size.
     */
    protected int getHandX (int size, int idx)
    {
        // get the card width from the image if not yet known
        if (_cardWidth == 0) {
            _cardWidth = getCardBackImage().getWidth();
        }
        // first compute the width of the entire hand, then use that to
        // determine the centered location
        int width = (size - 1) * _handSpacing + _cardWidth;
        return (_handLocation.x - width/2) + idx * _handSpacing;
    }
    
    /**
     * Determines the y location of the specified card sprite, given its
     * selection state.
     */
    protected int getHandY (CardSprite sprite)
    {
        if (_selectedHandSprites.contains(sprite)) {
            return _handLocation.y - _selectedCardOffset;
            
        } else if (isSelectable(sprite) &&
            (sprite == _activeCardSprite || isOnlySelectable(sprite))) {
            return _handLocation.y - _selectableCardOffset;
            
        } else {
            return _handLocation.y;
        }
    }
    
    /**
     * Given the current selection mode and predicate, determines if the
     * specified sprite is selectable.
     */
    protected boolean isSelectable (CardSprite sprite)
    {
        return _handSelectionMode != NONE &&
            (_handSelectionPredicate == null ||
                _handSelectionPredicate.evaluate(sprite));
    }
    
    /**
     * Determines whether the specified sprite is the only selectable sprite
     * in the hand.
     */
    protected boolean isOnlySelectable (CardSprite sprite)
    {
        int size = _handSprites.size();
        for (int i = 0; i < size; i++) {
            CardSprite cs = (CardSprite)_handSprites.get(i);
            if (cs != sprite && isSelectable(cs)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Lowers all board sprites so that they are rendered at or below the
     * specified layer.
     */
    protected void lowerBoardSprites (int layer)
    {
        // see if they're already lower
        int highest = getHighestBoardLayer();
        if (highest <= layer) {
            return;
        }
        
        // lower them just enough
        int size = _boardSprites.size(), adjustment = layer - highest;
        for (int i = 0; i < size; i++) {
            CardSprite cs = (CardSprite)_boardSprites.get(i);
            removeSprite(cs);
            cs.setRenderOrder(cs.getRenderOrder() + adjustment);
            addSprite(cs);
        }
    }
    
    /**
     * Returns the highest render order of any sprite on the board.
     */
    protected int getHighestBoardLayer ()
    {
        // must be at least zero, because that's the lowest number we can push
        // the sprites down to (the layer of the first card in the hand)
        int size = _boardSprites.size(), highest = 0;
        for (int i = 0; i < size; i++) {
            highest = Math.max(highest,
                ((CardSprite)_boardSprites.get(i)).getRenderOrder());
        }
        return highest;
    }
    
    /**
     * Clears an array of sprites from the specified list and from the panel.
     */
    protected void clearSprites (ArrayList sprites)
    {
        for (Iterator it = sprites.iterator(); it.hasNext(); ) {
            removeSprite((CardSprite)it.next());
            it.remove();
        }
    }
    
    /** The width of the playing cards. */
    protected int _cardWidth;
    
    /** The currently active card sprite (the one that the mouse is over). */
    protected CardSprite _activeCardSprite;
    
    /** The sprites for cards within the hand. */
    protected ArrayList _handSprites = new ArrayList();
    
    /** The sprites for cards within the hand that have been selected. */
    protected ArrayList _selectedHandSprites = new ArrayList();

    /** The current selection mode for the hand. */
    protected int _handSelectionMode;
    
    /** The predicate that determines which cards are selectable (if null, all
     * cards are selectable). */
    protected CardSpritePredicate _handSelectionPredicate;
    
    /** Observers of hand card selection/deselection. */
    protected ObserverList _handSelectionObservers = new ObserverList(
        ObserverList.FAST_UNSAFE_NOTIFY);
    
    /** The location of the center of the hand's upper edge. */
    protected Point _handLocation = new Point();
    
    /** The horizontal distance between cards in the hand. */
    protected int _handSpacing;
    
    /** The vertical distance to offset cards that are selectable. */
    protected int _selectableCardOffset;
    
    /** The vertical distance to offset cards that are selected. */
    protected int _selectedCardOffset;
    
    /** The sprites for cards on the board. */
    protected ArrayList _boardSprites = new ArrayList();
    
    /** A path observer that removes the sprite at the end of its path. */
    protected PathAdapter _pathEndRemover = new PathAdapter() {
        public void pathCompleted (Sprite sprite, Path path, long when) {
            removeSprite(sprite);
        }
    };
    
    /** Listens for interactions with cards in hand. */
    protected CardSpriteObserver _handSpriteObserver =
        new CardSpriteObserver() {
        public void cardSpriteClicked (CardSprite sprite, MouseEvent me) {
            // select, deselect, or play card in hand
            if (_selectedHandSprites.contains(sprite) &&
                _handSelectionMode != NONE) {
                deselectHandSprite(sprite);
                
            } else if (_handSprites.contains(sprite) && isSelectable(sprite)) {
                if (_handSelectionMode == PLAY_SINGLE) {
                    playHandSprite(sprite);
                
                } else {
                    selectHandSprite(sprite);
                }
            }
        }
        
        public void cardSpriteEntered (CardSprite sprite, MouseEvent me) {
            // update the offset
            if (_handSprites.contains(sprite)) {
                sprite.setLocation(sprite.getX(), getHandY(sprite));
            }
        }
        
        public void cardSpriteExited (CardSprite sprite, MouseEvent me) {
            // update the offset
            if (_handSprites.contains(sprite)) {
                sprite.setLocation(sprite.getX(), getHandY(sprite));
            }
        }
        
        public void cardSpriteDragged (CardSprite sprite, MouseEvent me) {
        }
    };
    
    /** Listens for mouse interactions with cards. */
    protected class CardListener extends MouseInputAdapter
    {
        public void mousePressed (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                _handleX = _activeCardSprite.getX() - me.getX();
                _handleY = _activeCardSprite.getY() - me.getY();        
                _hasBeenDragged = false;
            }
        }
        
        public void mouseReleased (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite) && 
                _hasBeenDragged) {
                _activeCardSprite.queueNotification(
                    new CardSpriteDraggedOp(_activeCardSprite, me)
                );
            }
        }
        
        public void mouseClicked (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                _activeCardSprite.queueNotification(
                    new CardSpriteClickedOp(_activeCardSprite, me)
                );
            }
        }
        
        public void mouseMoved (MouseEvent me)
        {
            Sprite newHighestHit = _spritemgr.getHighestHitSprite(
                me.getX(), me.getY());
                
            CardSprite newActiveCardSprite =
                (newHighestHit instanceof CardSprite ? 
                    (CardSprite)newHighestHit : null);
            
            if (_activeCardSprite != newActiveCardSprite) {
                if (_activeCardSprite != null &&
                    isManaged(_activeCardSprite)) {
                    _activeCardSprite.queueNotification(
                        new CardSpriteExitedOp(_activeCardSprite, me)
                    );
                }
                _activeCardSprite = newActiveCardSprite;
                if (_activeCardSprite != null) {
                    _activeCardSprite.queueNotification(
                        new CardSpriteEnteredOp(_activeCardSprite, me)
                    );
                }
            }
        }
        
        public void mouseDragged (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite) &&
                _activeCardSprite.isDraggable()) {
                _activeCardSprite.setLocation(
                    me.getX() + _handleX,
                    me.getY() + _handleY
                ); 
                _hasBeenDragged = true;
                
            } else {
                mouseMoved(me);
            }
        }
        
        protected int _handleX, _handleY;
        protected boolean _hasBeenDragged;
    }
    
    /** Calls CardSpriteObserver.cardSpriteClicked. */ 
    protected static class CardSpriteClickedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteClickedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteClicked(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteEntered. */ 
    protected static class CardSpriteEnteredOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteEnteredOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteEntered(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteExited. */ 
    protected static class CardSpriteExitedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteExitedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteExited(_sprite, _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteDragged. */
    protected static class CardSpriteDraggedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteDraggedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteDragged(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }

    /** A nice default green card table background color. */
    protected static Color DEFAULT_BACKGROUND = new Color(0x326D36);
}
