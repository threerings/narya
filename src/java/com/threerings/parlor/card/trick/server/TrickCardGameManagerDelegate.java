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

package com.threerings.parlor.card.trick.server;

import java.util.ArrayList;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.Interval;

import com.threerings.util.Name;
import com.threerings.util.RandomUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

import com.threerings.parlor.card.Log;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardGameObject;
import com.threerings.parlor.card.data.Deck;
import com.threerings.parlor.card.data.Hand;
import com.threerings.parlor.card.data.PlayerCard;
import com.threerings.parlor.card.server.CardGameManager;
import com.threerings.parlor.card.trick.data.TrickCardGameMarshaller;
import com.threerings.parlor.card.trick.data.TrickCardGameObject;

/**
 * A card game manager delegate for trick-based card games, such as
 * Spades and Hearts.
 */
public class TrickCardGameManagerDelegate extends TurnGameManagerDelegate
    implements TrickCardGameProvider
{   
    /**
     * Constructor.
     *
     * @param manager the game manager
     */
    public TrickCardGameManagerDelegate (CardGameManager manager)
    {
        super(manager);
        
        _cgmgr = manager;
        _deck = new Deck();
    }
    
    // Documentation inherited.
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        
        _trickCardGame = (TrickCardGameObject)plobj;
        
        _cardGame = (CardGameObject)plobj;
        
        _trickCardGame.setTrickCardGameService(
            (TrickCardGameMarshaller)PresentsServer.invmgr.registerDispatcher(
                new TrickCardGameDispatcher(this), false));
    }
    
    // Documentation inherited.
    public void didShutdown ()
    {
        super.didShutdown();
        
        PresentsServer.invmgr.clearDispatcher(
            _trickCardGame.getTrickCardGameService());
    }
    
    // Documentation inherited.
    public void gameWillStart ()
    {
        super.gameWillStart();
        
        // clear out the last cards played
        _trickCardGame.setLastCardsPlayed(null);
    }
    
    /**
     * Called when the game has started.  Default implementation starts the
     * first hand.
     */
    public void gameDidStart ()
    {
        super.gameDidStart();
        
        // start the first hand
        startHand();
    }
    
    // Documentation inherited.
    public void gameWillEnd ()
    {
        // make sure all intervals are cancelled
        _turnTimeoutInterval.cancel();
        _endTrickInterval.cancel();
        
        // make sure trick state is back to between hands
        if (_trickCardGame.getTrickState() !=
            TrickCardGameObject.BETWEEN_HANDS) {
            _trickCardGame.setTrickState(TrickCardGameObject.BETWEEN_HANDS);
        }
        
        // initialize the array of rematch requests
        _trickCardGame.setRematchRequests(
            new int[_cardGame.getPlayerCount()]);
        
        super.gameDidEnd();
    }
    
    // Documentation inherited.
    public void startTurn ()
    {
        super.startTurn();
        
        // schedule the timeout interval
        _turnTimeoutInterval.schedule(getTurnTimeoutDelay());
    }
    
    // Documentation inherited.
    public void endTurn ()
    {
        // cancel the timeout interval
        _turnTimeoutInterval.cancel();
        
        super.endTurn();
    }
    
    /**
     * Starts a hand of cards.  Calls {@link #handWillStart}, sets the trick
     * state to PLAYING_HAND, and calls {@link #handDidStart}.
     */
    public void startHand ()
    {
        handWillStart();
        _trickCardGame.setTrickState(TrickCardGameObject.PLAYING_HAND);
        handDidStart();
    }
    
    /**
     * Ends the hand of cards.  Calls {@link #handWillEnd}, sets the trick
     * state to BETWEEN_HANDS, and calls {@link #handDidEnd}.
     */
    public void endHand ()
    {
        handWillEnd();
        _trickCardGame.setTrickState(TrickCardGameObject.BETWEEN_HANDS);
        handDidEnd();
    }
    
    /**
     * Starts a trick.  Calls {@link #trickWillStart}, sets the trick
     * state to PLAYING_TRICK, and calls {@link #trickDidStart}.
     */
    public void startTrick ()
    {
        trickWillStart();
        _trickCardGame.setTrickState(TrickCardGameObject.PLAYING_TRICK);
        trickDidStart();
    }
    
    /**
     * Ends the trick.  Calls {@link #trickWillEnd}, sets the trick
     * state to PLAYING_HAND, and calls {@link #trickDidEnd}.
     */
    public void endTrick ()
    {
        trickWillEnd();
        _trickCardGame.setTrickState(TrickCardGameObject.PLAYING_HAND);
        trickDidEnd();
    }
    
    /**
     * Processes a request to transfer a group of cards between players.
     * Default implementation verifies that the user's hand contains the
     * specified cards, then transfers the cards (letting everyone know
     * that the transfer has taken place).
     */
    public void sendCardsToPlayer (ClientObject client, int toidx,
        Card[] cards)
        throws InvocationException
    {
        // make sure they're actually a player
        int fromidx = _cgmgr.getPlayerIndex(client);
        if (fromidx == -1) {
            throw new InvocationException("m.not_playing");
        }
        
        // make sure they have the cards
        if (!_hands[fromidx].containsAll(cards)) {
            throw new InvocationException("m.not_holding_card");
        }
        
        // remove from sending player's hand
        _hands[fromidx].removeAll(cards);
        
        // add to receiving player's hand
        _hands[toidx].addAll(cards);
        
        // notify everyone of the transfer
        _cgmgr.transferCardsBetweenPlayers(fromidx, toidx, cards);
    }
    
    // Documentation inherited.
    public void playCard (ClientObject client, Card card)
        throws InvocationException
    {
        // make sure we're playing a trick
        if (_trickCardGame.getTrickState() !=
            TrickCardGameObject.PLAYING_TRICK) {
            throw new InvocationException("not_playing_trick");
        }
        
        // make sure it's their turn
        Name username = ((BodyObject)client).username;
        verifyPlayersTurn(username);
        
        // make sure their hand contains the specified card
        int pidx = _cardGame.getPlayerIndex(username);
        if (!_hands[pidx].contains(card)) {
            throw new InvocationException("m.not_holding_card");
        }
        
        // make sure the card is legal to play
        if (!_trickCardGame.isCardPlayable(_hands[pidx], card)) {
            throw new InvocationException("m.card_not_playable");
        }
        
        // play the card by removing it from the hand and adding it to the end
        // of the cards played array
        _hands[pidx].remove(card);
        PlayerCard[] cards = (PlayerCard[])ArrayUtil.append(
            _trickCardGame.getCardsPlayed(), new PlayerCard(pidx, card));
        _trickCardGame.setCardsPlayed(cards);
        
        // end the user's turn
        endTurn();
        
        // end the trick if everyone has played a card
        if (_turnIdx == -1) {
            if (_endTrickDelay == 0) {
                endTrick();
                
            } else {
                _endTrickInterval.schedule(_endTrickDelay);
            }
        }
    }
    
    // Documentation inherited.
    public void requestRematch (ClientObject client)
        throws InvocationException
    {
        // make sure the game is over
        if (_cardGame.state != CardGameObject.GAME_OVER) {
            throw new InvocationException("m.game_not_over");
        }
        
        // make sure the requester is one of the players
        int pidx = _cgmgr.getPlayerIndex(client);
        if (pidx == -1) {
            throw new InvocationException("m.not_playing");
        }
        
        // make sure the player hasn't already requested
        if (_trickCardGame.getRematchRequests()[pidx] !=
            TrickCardGameObject.NO_REQUEST) {
            throw new InvocationException("m.already_requested");
        }
        
        // if player is first requesting, set to request; else set
        // to accept
        int req = (getRematchRequestCount() == 0 ?
            TrickCardGameObject.REQUESTS_REMATCH :
            TrickCardGameObject.ACCEPTS_REMATCH);
        _trickCardGame.setRematchRequestsAt(req, pidx);
        
        // if all players accept the rematch, restart the game
        if (getRematchRequestCount() == _cardGame.getPlayerCount()) {
            _cgmgr.rematchGame();
        }
    }
    
    /**
     * Returns the number of players currently requesting or accepting
     * a rematch.
     */
    protected int getRematchRequestCount ()
    {
        int[] rematchRequests = _trickCardGame.getRematchRequests();
        int count = 0;
        for (int i = 0; i < rematchRequests.length; i++) {
            if (rematchRequests[i] != TrickCardGameObject.NO_REQUEST) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks whether the trick is complete--that is, whether each player has
     * played a card.
     */
    protected boolean isTrickComplete ()
    {
        return _trickCardGame.getCardsPlayed().length ==
            _cardGame.getPlayerCount();
    }
    
    /**
     * Throws an {@link InvocationException} if it is not the specified
     * user's turn.
     */
    protected void verifyPlayersTurn (Name username)
        throws InvocationException
    {
        if (!username.equals(_trickCardGame.getTurnHolder()) {
            throw new InvocationException("m.not_your_turn");
        }
    }
    
    // Documentation inherited.
    protected void setFirstTurnHolder ()
    {
        if (_trickCardGame.getTrickState() ==
                TrickCardGameObject.PLAYING_TRICK) {
            super.setFirstTurnHolder();
            
        } else {
            _turnIdx = -1;
        }
    }
    
    // Documentation inherited.
    protected void setNextTurnHolder ()
    {
        if (_trickCardGame.getTrickState() ==
                TrickCardGameObject.PLAYING_TRICK &&
            !isTrickComplete()) {
            super.setNextTurnHolder();
            
        } else {
            _turnIdx = -1;
        }
    }
    
    /**
     * Returns the amount of time to allow before timing out the turn
     * by calling {@link #turnTimedOut}.  Default implementation returns
     * a minute.
     */
    protected long getTurnTimeoutDelay ()
    {
        return 60 * 1000L;
    }
    
    /**
     * Called when the current turn times out.  Default implementation
     * plays a random playable card if in the trick-playing state.
     */
    protected void turnTimedOut ()
    {
        if (_trickCardGame.getTrickState() ==
            TrickCardGameObject.PLAYING_TRICK) {
            int pidx = _cardGame.getPlayerIndex(
                _trickCardGame.getTurnHolder());
            Card card = pickRandomPlayableCard(_hands[pidx]);
            try {
                playCard(_cgmgr.getClientObject(pidx), card);
                
            } catch (InvocationException ie) {
                Log.warning("Couldn't play card [card=" + card + ", hand=" +
                    _hands[pidx] + "].");
                Log.logStackTrace(ie);
            }
        }
    }
    
    /**
     * Returns a random playable card from the specified hand.
     */
    protected Card pickRandomPlayableCard (Hand hand)
    {
        ArrayList playableCards = new ArrayList();
        for (int i = 0; i < hand.size(); i++) {
            Card card = (Card)hand.get(i);
            if (_trickCardGame.isCardPlayable(hand, card)) {
                playableCards.add(card);
            }
        }
        return (Card)RandomUtil.pickRandom(playableCards);
    }
    
    /**
     * Notifies the object that a new hand is about to start.
     */
    protected void handWillStart ()
    {}
    
    /**
     * Notifies the object that a new hand has just started.  Default
     * implementation prepares the deck, deals the hands, and starts the
     * first trick.
     */
    protected void handDidStart ()
    {
        // prepare the deck
        prepareDeck();
        
        // deal cards to players
        dealHands();
        
        // start the first trick
        startTrick();
    }
    
    /**
     * Prepares the deck for a new hand of cards.  Default implementation
     * resets to a full deck without jokers and shuffles.
     */
    protected void prepareDeck ()
    {
        _deck.reset(false);
        _deck.shuffle();
    }
    
    /**
     * Deals hands to the players.  Default implementation deals the entire
     * deck to the players in equal-sized hands.
     */
    protected void dealHands ()
    {
        _hands = _cgmgr.dealHands(_deck, _deck.size() /
            _cardGame.getPlayerCount());
    }
    
    /**
     * Notifies the object that the hand is about to end.
     */
    protected void handWillEnd ()
    {}
    
    /**
     * Notifies the object that the hand has ended.  Default implementation
     * starts the next hand.
     */
    protected void handDidEnd ()
    {
        startHand();
    }
    
    /**
     * Notifies the object that a new trick is about to start.  Default
     * implementation resets the array of cards played.
     */
    protected void trickWillStart ()
    {
        _trickCardGame.setCardsPlayed(new PlayerCard[0]);
    }
    
    /**
     * Notifies the object that a new trick has started.  Default
     * implementation sets the first turn holder and starts the
     * turn.
     */
    protected void trickDidStart ()
    {
        setFirstTurnHolder();
        startTurn();
    }
    
    /**
     * Notifies the object that the trick is about to end.
     */
    protected void trickWillEnd ()
    {}
    
    /**
     * Notifies the object that the trick has ended.  Default implementation
     * records the last cards played and starts the next trick, unless a
     * player has run out of cards, in which case it ends the hand.
     */
    protected void trickDidEnd ()
    {
        // store the trick results for late-joiners
        _trickCardGame.setLastCardsPlayed(_trickCardGame.getCardsPlayed());
        
        // clear out the cards played in the trick
        _trickCardGame.setCardsPlayed(null);
        
        // verify that each player has at least one card
        if (anyHandsEmpty()) {
            endHand();
            return;
        }
        
        // everyone has cards; let's play another trick
        startTrick();
    }
    
    /**
     * Checks whether any hands are empty.
     */
    protected boolean anyHandsEmpty ()
    {
        for (int i = 0; i < _hands.length; i++) {
            if (_hands[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /** The card game manager. */
    protected CardGameManager _cgmgr;
    
    /** The game object as trick card game. */
    protected TrickCardGameObject _trickCardGame;
    
    /** The game object as card game. */
    protected CardGameObject _cardGame;
    
    /** The amount of time to wait before ending the trick. */
    protected long _endTrickDelay;
    
    /** The deck from which cards are dealt. */
    protected Deck _deck;
    
    /** The hands of each player. */
    protected Hand[] _hands;
    
    /** The all-purpose turn timeout interval.  */
    protected Interval _turnTimeoutInterval =
        new Interval(PresentsServer.omgr) {
        public void expired () {
            turnTimedOut();
        }
    };
    
    /** Calls {@link #endTrick} upon expiration. */
    protected Interval _endTrickInterval = new Interval(PresentsServer.omgr) {
        public void expired () {
            endTrick();
        }
    };
}
