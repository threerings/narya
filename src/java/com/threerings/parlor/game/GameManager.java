//
// $Id: GameManager.java,v 1.67 2003/07/16 18:03:30 ray Exp $

package com.threerings.parlor.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.samskivert.util.IntervalManager;
import com.samskivert.util.StringUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.util.SafeInterval;

import com.threerings.crowd.chat.server.SpeakProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManagerDelegate;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.server.ParlorSender;

/**
 * The game manager handles the server side management of a game. It
 * manipulates the game state in accordance with the logic of the game
 * flow and generally manages the whole game playing process.
 *
 * <p> The game manager extends the place manager because games are
 * implicitly played in a location, the players of the game implicitly
 * bodies in that location.
 */
public class GameManager extends PlaceManager
    implements ParlorCodes, GameProvider, AttributeChangeListener
{
    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // save off a casted reference to our config
        _gameconfig = (GameConfig)_config;

        // register this game manager
        _managers.add(this);

        // and start up a tick interval if we've not already got one
        if (_tiid == -1) {
            _tiid = IntervalManager.register(
                new SafeInterval(CrowdServer.omgr) {
                    public void run () {
                        tickAllGames();
                    }
                }, TICK_DELAY, null, true);
        }
    }

    /**
     * Returns the configuration object for the game being managed by this
     * manager.
     */
    public GameConfig getGameConfig ()
    {
        return _gameconfig;
    }

    /**
     * Adds the given player to the game at the first available player
     * index.  This should only be called before the game is started, and
     * is most likely to be used to add players to party games.
     *
     * @param player the username of the player to add to this game.
     * @return the player index at which the player was added, or
     * <code>-1</code> if the player could not be added to the game.
     */
    public int addPlayer (String player)
    {
        // determine the first available player index
        int pidx = -1;
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            if (!_gameobj.isOccupiedPlayer(ii)) {
                pidx = ii;
                break;
            }
        }

        // sanity-check the player index
        if (pidx == -1) {
            Log.warning("Couldn't find free player index for player  " +
                        "[game=" + _gameobj.which() + ", player=" + player +
                        ", players=" + StringUtil.toString(_gameobj.players) +
                        "].");
            return -1;
        }

        // proceed with the rest of the adding business
        return (!addPlayerAt(player, pidx)) ? -1 : pidx;
    }

    /**
     * Adds the given player to the game at the specified player index.
     * This should only be called before the game is started, and is most
     * likely to be used to add players to party games.
     *
     * @param player the username of the player to add to this game.
     * @param pidx the player index at which the player is to be added.
     * @return true if the player was added successfully, false if not.
     */
    public boolean addPlayerAt (String player, int pidx)
    {
        // make sure the specified player index is valid
        if (pidx < 0 || pidx >= getPlayerSlots()) {
            Log.warning("Attempt to add player at an invalid index " +
                        "[game=" + _gameobj.which() + ", player=" + player +
                        ", pidx=" + pidx + "].");
            return false;
        }

        // make sure the player index is available
        if (_gameobj.players[pidx] != null) {
            Log.warning("Attempt to add player at occupied index " +
                        "[game=" + _gameobj.which() + ", player=" + player +
                        ", pidx=" + pidx + "].");
            return false;
        }

        // make sure the player isn't already somehow a part of the game
        // to avoid any potential badness that might ensue if we added
        // them more than once
        if (_gameobj.getPlayerIndex(player) != -1) {
            Log.warning("Attempt to add player to game that they're already " +
                        "playing [game=" + _gameobj.which() +
                        ", player=" + player + "].");
            return false;
        }

        // get the player's body object
        BodyObject bobj = CrowdServer.lookupBody(player);
        if (bobj == null) {
            Log.warning("Unable to get body object while adding player " +
                        "[game=" + _gameobj.which() +
                        ", player=" + player + "].");
            return false;
        }

        // fill in the player's information
        _gameobj.setPlayersAt(player, pidx);

        // increment the number of players in the game
        _playerCount++;

        // save off their oid
        _playerOids[pidx] = bobj.getOid();

        // let derived classes do what they like
        playerWasAdded(player, pidx);

        return true;
    }

    /**
     * Called when a player was added to the game.  Derived classes may
     * override this method to perform any game-specific actions they
     * desire, but should be sure to call
     * <code>super.playerWasAdded()</code>.
     *
     * @param player the username of the player added to the game.
     * @param pidx the player index of the player added to the game.
     */
    protected void playerWasAdded (String player, int pidx)
    {
    }

    /**
     * Removes the given player from the game.  This is most likely to be
     * used to allow players involved in a party game to leave the game
     * early-on if they realize they'd rather not play for some reason.
     *
     * @param player the username of the player to remove from this game.
     * @return true if the player was successfully removed, false if not.
     */
    public boolean removePlayer (String player)
    {
        // get the player's index in the player list
        int pidx = _gameobj.getPlayerIndex(player);

        // sanity-check the player index
        if (pidx == -1) {
            Log.warning("Attempt to remove non-player from players list " +
                        "[game=" + _gameobj.which() +
                        ", player=" + player +
                        ", players=" + StringUtil.toString(_gameobj.players) +
                        "].");
            return false;
        }

        // remove the player from the players list
        _gameobj.setPlayersAt(null, pidx);

        // clear out the player's entry in the player oid list
        _playerOids[pidx] = 0;

        if (_AIs != null) {
            // clear out the player's entry in the AI list
            _AIs[pidx] = -1;
        }

        // decrement the number of players in the game
        _playerCount--;

        // let derived classes do what they like
        playerWasRemoved(player, pidx);

        return true;
    }

    /**
     * Called when a player was removed from the game.  Derived classes
     * may override this method to perform any game-specific actions they
     * desire, but should be sure to call
     * <code>super.playerWasRemoved()</code>.
     *
     * @param player the username of the player removed from the game.
     * @param pidx the player index of the player before they were removed
     * from the game.
     */
    protected void playerWasRemoved (String player, int pidx)
    {
    }

    /**
     * Sets the specified player as an AI with the specified skill.  It is
     * assumed that this will be set soon after the player names for all
     * AIs present in the game. (It should be done before human players
     * start trickling into the game.)
     *
     * @param pidx the player index of the AI.
     * @param skill the skill level, from 0 to 100 inclusive.
     */
    public void setAI (final int pidx, final byte skill)
    {
        if (_AIs == null) {
            // create and initialize the AI skill level array
            _AIs = new byte[getPlayerSlots()];
            Arrays.fill(_AIs, (byte)-1);
            // set up a delegate op for AI ticking
            _tickAIOp = new TickAIDelegateOp();
        }

        // save off the AI's skill level
        _AIs[pidx] = skill;

        // let the delegates know that the player's been made an AI
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).setAI(pidx, skill);
            }
        });
    }

    /**
     * Returns the name of the player with the specified index.
     */
    public String getPlayerName (int index)
    {
        return (_gameobj == null) ? "" : _gameobj.players[index];
    }

    /**
     * Returns the player index of the given user in the game, or 
     * <code>-1</code> if the player is not involved in the game.
     */
    public int getPlayerIndex (String username)
    {
        return (_gameobj == null) ? -1 : _gameobj.getPlayerIndex(username);
    }

    /**
     * Returns the user object oid of the player with the specified index.
     */
    public int getPlayerOid (int index)
    {
        return (_playerOids == null) ? -1 : _playerOids[index];
    }

    /**
     * Returns the number of players in the game.
     */
    public int getPlayerCount ()
    {
        return _playerCount;
    }

    /**
     * Returns the number of players allowed in this game.
     */
    public int getPlayerSlots ()
    {
        return _gameconfig.players.length;
    }

    /**
     * Returns whether the player at the specified player index is an AI.
     */
    public boolean isAI (int pidx)
    {
        return (_AIs != null && _AIs[pidx] != -1);
    }

    /**
     * Returns the unique round identifier for the current round.
     */
    public int getRoundId ()
    {
        return _gameobj.roundId;
    }

    /**
     * Sends a system message to the players in the game room.
     */
    public void systemMessage (String msgbundle, String msg)
    {
        systemMessage(msgbundle, msg, false);
    }

    /**
     * Sends a system message to the players in the game room.
     *
     * @param waitForStart if true, the message will not be sent until the
     * game has started.
     */
    public void systemMessage (
        String msgbundle, String msg, boolean waitForStart)
    {
        if (waitForStart &&
            ((_gameobj == null) ||
             (_gameobj.state == GameObject.AWAITING_PLAYERS))) {
            // queue up the message.
            if (_startmsgs == null) {
                _startmsgs = new ArrayList();
            }
            _startmsgs.add(msgbundle);
            _startmsgs.add(msg);
            return;
        }

        // otherwise, just deliver the message
        SpeakProvider.sendInfo(_gameobj, msgbundle, msg);
    }

    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return GameObject.class;
    }

    // documentation inherited
    protected void didStartup ()
    {
        // obtain a casted reference to our game object
        _gameobj = (GameObject)_plobj;

        // stick the players into the game object
        _gameobj.setPlayers(_gameconfig.players);

        // save off the number of players so that we needn't repeatedly
        // iterate through the player name array server-side unnecessarily
        _playerCount = _gameobj.getPlayerCount();

        // instantiate a player oid array which we'll fill in later
        _playerOids = new int[getPlayerSlots()];

        // create and fill in our game service object
        GameMarshaller service = (GameMarshaller)
            _invmgr.registerDispatcher(new GameDispatcher(this), false);
        _gameobj.setGameService(service);

        // give delegates a chance to do their thing
        super.didStartup();

        // let the players of this game know that we're ready to roll (if
        // we have a specific set of players)
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            // skip non-existent players
            if (!_gameobj.isOccupiedPlayer(ii)) {
                continue;
            }

            BodyObject bobj = CrowdServer.lookupBody(_gameobj.players[ii]);
            if (bobj == null) {
                Log.warning("Unable to deliver game ready to non-existent " +
                            "player [game=" + _gameobj.which() +
                            ", player=" + _gameobj.players[ii] + "].");
                continue;
            }

            // deliver a game ready notification to the player
            ParlorSender.gameIsReady(bobj, _gameobj.getOid());
        }

        // start up a no-show timer if needed
        if (needsNoShowTimer()) {
            IntervalManager.register(new SafeInterval(CrowdServer.omgr) {
                public void run () {
                    checkForNoShows();
                }
            }, NOSHOW_DELAY, null, false);
        }
    }

    /**
     * Returns true if this game requires a no-show timer. The default
     * implementation returns true for non-party games and false for party
     * games. Derived classes may wish to change or augment this behavior.
     */
    protected boolean needsNoShowTimer ()
    {
        return !isPartyGame();
    }

    // documentation inherited
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister this game manager
        _managers.remove(this);

        // remove the tick interval if there are no remaining managers
        if (_managers.size() == 0) {
            IntervalManager.remove(_tiid);
            _tiid = -1;
        }

        // clear out our service registration
        _invmgr.clearDispatcher(_gameobj.gameService);
    }

    // documentation inherited
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // TODO: if this is a party game not yet in play and the creator
        // left, choose a new creator if at least one player still remains

        // deal with disappearing players
    }

    /**
     * When a game room becomes empty, we cancel the game if it's still in
     * progress and close down the game room.
     */
    protected void placeBecameEmpty ()
    {
//         Log.info("Game room empty. Going away. " +
//                  "[game=" + _gameobj.which() + "].");

        // cancel the game if it was not already over
        if (_gameobj.state != GameObject.GAME_OVER) {
            _gameobj.setState(GameObject.CANCELLED);
        }

        // shut down the place (which will destroy the game object and
        // clean up after everything)
        shutdown();
    }

    /**
     * Called when all players have arrived in the game room. By default,
     * this starts up the game, but a manager may wish to override this
     * and start the game according to different criterion.
     */
    protected void playersAllHere ()
    {
        // start up the game if we're not a party game and if we haven't
        // already done so
        if (!isPartyGame() &&
            _gameobj.state == GameObject.AWAITING_PLAYERS) {
            startGame();
        }
    }

    /**
     * Called after the no-show delay has expired following the delivery
     * of notifications to all players that the game is ready.
     * <em>Note:</em> this is not called for party games. Those games have
     * a human who decides when to start the game.
     */
    protected void checkForNoShows ()
    {
        // nothing to worry about if we're already started
        if (_gameobj.state != GameObject.AWAITING_PLAYERS) {
            return;
        }

        // if there's no one in the room, go ahead and clear it out
        if (_plobj.occupants.size() == 0) {
            Log.info("Cancelling total no-show " +
                     "[game=" + _gameobj.which() +
                     ", players=" + StringUtil.toString(_gameobj.players) +
                     ", poids=" + StringUtil.toString(_playerOids) + "].");
            placeBecameEmpty();

        } else {
            // do the right thing if we have any no-show players
            for (int ii = 0; ii < getPlayerSlots(); ii++) {
                if (!playerIsReady(ii)) {
                    handlePartialNoShow();
                    return;
                }
            }
        }
    }

    /**
     * This is called when some, but not all, players failed to show up
     * for a game. The default implementation simply cancels the game.
     */
    protected void handlePartialNoShow ()
    {
        // cancel the game
        _gameobj.setState(GameObject.CANCELLED);
    }

    /**
     * This is called when the game is ready to start (all players
     * involved have delivered their "am ready" notifications). It calls
     * {@link #gameWillStart}, sets the necessary wheels in motion and
     * then calls {@link #gameDidStart}.  Derived classes should override
     * one or both of the calldown functions (rather than this function)
     * if they need to do things before or after the game starts.
     *
     * @return true if the game was started, false if it could not be
     * started because it was already in play or because all players have
     * not yet reported in.
     */
    public boolean startGame ()
    {
        // complain if we're already started
        if (_gameobj.state == GameObject.IN_PLAY) {
            Log.warning("Requested to start an already in-play game " +
                        "[game=" + _gameobj.which() + "].");
            Thread.dumpStack();
            return false;
        }

        // make sure everyone has turned up
        if (!allPlayersReady()) {
            Log.warning("Requested to start a game that is still " +
                        "awaiting players [game=" + _gameobj.which() +
                        ", pnames=" + StringUtil.toString(_gameobj.players) +
                        ", poids=" + StringUtil.toString(_playerOids) + "].");
            return false;
        }

        // let the derived class do its pre-start stuff
        gameWillStart();

        // transition the game to started
        _gameobj.setState(GameObject.IN_PLAY);

        // when our events are applied, we'll call gameDidStart()
        return true;
    }

    /**
     * Called when the game is about to start, but before the game start
     * notification has been delivered to the players. Derived classes
     * should override this if they need to perform some pre-start
     * activities, but should be sure to call
     * <code>super.gameWillStart()</code>.
     */
    protected void gameWillStart ()
    {
        // increment the round identifier
        _gameobj.setRoundId(_gameobj.roundId + 1);

        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillStart();
            }
        });
    }

    /**
     * Called after the game start notification was dispatched.  Derived
     * classes can override this to put whatever wheels they might need
     * into motion now that the game is started (if anything other than
     * transitioning the game to {@link GameObject#IN_PLAY} is necessary),
     * but should be sure to call <code>super.gameDidStart()</code>.
     */
    protected void gameDidStart ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidStart();
            }
        });

        // inform the players of any pending messages.
        if (_startmsgs != null) {
            for (Iterator iter = _startmsgs.iterator(); iter.hasNext(); ) {
                systemMessage((String) iter.next(), // bundle
                              (String) iter.next()); // message
            }
            _startmsgs = null;
        }

        // and register ourselves to receive AI ticks
        if (_AIs != null) {
            AIGameTicker.registerAIGame(this);
        }
    }

    /**
     * Called by the {@link AIGameTicker} if we're registered as an AI
     * game.
     */
    protected void tickAIs ()
    {
        for (int ii = 0; ii < _AIs.length; ii++) {
            byte level = _AIs[ii];
            if (level != -1) {
                tickAI(ii, level);
            }
        }
    }

    /**
     * Called by tickAIs to tick each AI in the game.
     */
    protected void tickAI (int pidx, byte level)
    {
        _tickAIOp.setAI(pidx, level);
        applyToDelegates(_tickAIOp);
    }

    /**
     * Called when the game is known to be over. This will call some
     * calldown functions to determine the winner of the game and then
     * transition the game to the {@link GameObject#GAME_OVER} state.
     */
    public void endGame ()
    {
        if (_gameobj.state != GameObject.IN_PLAY) {
            Log.debug("Refusing to end game that was not in play " +
                      "[game=" + _gameobj.which() + "].");
            return;
        }

        _gameobj.startTransaction();
        try {

            // let the derived class do its pre-end stuff
            gameWillEnd();

            // determine winners and set them in the game object
            boolean[] winners = new boolean[getPlayerSlots()];
            assignWinners(winners);
            _gameobj.setWinners(winners);

            // transition to the game over state
            _gameobj.setState(GameObject.GAME_OVER);

        } finally {
            _gameobj.commitTransaction();
        }

        // wait until we hear the game state transition on the game object
        // to invoke our game over code so that we can be sure that any
        // final events dispatched on the game object prior to the call to
        // endGame() have been dispatched
    }

    /**
     * Assigns the final winning status for each player to their respect
     * player index in the supplied array.  This will be called by {@link
     * #endGame} when the game is over.  The default implementation marks
     * no players as winners.  Derived classes should override this method
     * in order to customize the winning conditions.
     */
    protected void assignWinners (boolean[] winners)
    {
        Arrays.fill(winners, false);
    }

    /**
     * Called when the game is about to end, but before the game end
     * notification has been delivered to the players.  Derived classes
     * should override this if they need to perform some pre-end
     * activities, but should be sure to call
     * <code>super.gameWillEnd()</code>.
     */
    protected void gameWillEnd ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillEnd();
            }
        });
    }

    /**
     * Called after the game has transitioned to the {@link
     * GameObject#GAME_OVER} state. Derived classes should override this
     * to perform any post-game activities, but should be sure to call
     * <code>super.gameDidEnd()</code>.
     */
    protected void gameDidEnd ()
    {
        // remove ourselves from the AI ticker, if applicable
        if (_AIs != null) {
            AIGameTicker.unregisterAIGame(this);
        }

        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidEnd();
            }
        });

        // calculate ratings and all that...
    }

    /**
     * Called when the game is to be reset to its starting state in
     * preparation for a new game without actually ending the current
     * game. It calls {@link #gameWillReset} followed by the standard game
     * start processing ({@link #gameWillStart} and {@link
     * #gameDidStart}). Derived classes should override these calldown
     * functions (rather than this function) if they need to do things
     * before or after the game resets.
     */
    public void resetGame ()
    {
        // let the derived class do its pre-reset stuff
        gameWillReset();
        // do the standard game start processing
        gameWillStart();
        // transition to in-play which will trigger a call to gameDidStart()
        _gameobj.setState(GameObject.IN_PLAY);
    }

    /**
     * Called when the game is about to reset, but before the board has
     * been re-initialized or any other clearing out of game data has
     * taken place.  Derived classes should override this if they need to
     * perform some pre-reset activities.
     */
    protected void gameWillReset ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillReset();
            }
        });
    }

    // documentation inherited from interface
    public void playerReady (ClientObject caller)
    {
        BodyObject plobj = (BodyObject)caller;

        // get the user's player index
        int pidx = _gameobj.getPlayerIndex(plobj.username);
        if (pidx == -1) {
            // only complain if this is not a party game, since it's
            // perfectly normal to receive a player ready notification
            // from a user entering a party game in which they're not yet
            // a participant
            if (!isPartyGame()) {
                Log.warning("Received playerReady() from non-player? " +
                            "[caller=" + caller + "].");
            }
            return;
        }

        // make a note of this player's oid
        _playerOids[pidx] = plobj.getOid();

        // if everyone is now ready to go, get things underway
        if (allPlayersReady()) {
            playersAllHere();
        }
    }

    /**
     * Returns true if all (non-AI) players have delivered their {@link
     * #playerReady} notifications, false if they have not.
     */
    public boolean allPlayersReady ()
    {
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            if (!playerIsReady(ii)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the player at the specified slot is ready (or if
     * there is meant to be no player in that slot), false if there is
     * meant to be a player in the specified slot and they have not yet
     * reported that they are ready.
     */
    public boolean playerIsReady (int pidx)
    {
        return (!_gameobj.isOccupiedPlayer(pidx) ||  // unoccupied slot
                _playerOids[pidx] != 0 ||            // player is ready
                (_AIs != null && _AIs[pidx] != -1)); // player is AI
    }

    // documentation inherited from interface
    public void startPartyGame (ClientObject caller)
    {
        // make sure this is a party game
        if (!isPartyGame()) {
            Log.warning("Attempt to player-start a non-party game " +
                        "[game=" + _gameobj.which() +
                        ", caller=" + caller + "].");
            return;
        }

        // make sure the caller is the creating player
        BodyObject plobj = (BodyObject)caller;
        int pidx = _gameobj.getPlayerIndex(plobj.username);
        if (pidx != _gameobj.creator) {
            Log.warning("Attempt to start party game by non-creating player " +
                        "[game=" + _gameobj.which() +
                        ", creator=" + getPlayerName(_gameobj.creator) +
                        ", caller=" + caller + "].");
            return;
        }

        // make sure the game is ready to go
        if (!canStartPartyGame()) {
            Log.warning("Attempt to start party game that can't yet begin " +
                        "[game=" + _gameobj.which() +
                        ", caller=" + caller + "].");
            return;
        }

        // start things up
        startGame();
    }

    /**
     * Returns whether this party game is all set to begin.  The default
     * implementation returns true.  Derived classes that implement a
     * party game should override this method to enforce any prerequisites
     * (such as a minimum number of players) as appropriate.
     */
    protected boolean canStartPartyGame ()
    {
        return true;
    }

    /**
     * Gives game managers an opportunity to perform periodic processing
     * that is not driven by events generated by the player.
     */
    protected void tick (long tickStamp)
    {
        // nothing for now
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(GameObject.STATE)) {
            switch (event.getIntValue()) {
            case GameObject.IN_PLAY:
                gameDidStart();
                break;

            case GameObject.CANCELLED:
                // fall through to GAME_OVER case
            case GameObject.GAME_OVER:
                // now we do our end of game processing
                gameDidEnd();
                break;
            }
        }
    }

    /**
     * Called periodically to call {@link #tick} on all registered game
     * managers.
     */
    protected static void tickAllGames ()
    {
        long now = System.currentTimeMillis();
        int size = _managers.size();
        for (int ii = 0; ii < size; ii++) {
            GameManager gmgr = (GameManager)_managers.get(ii);
            try {
                gmgr.tick(now);
            } catch (Exception e) {
                Log.warning("Game manager choked during tick " +
                            "[gmgr=" + gmgr + "].");
                Log.logStackTrace(e);
            }
        }
    }

    /**
     * A helper operation to distribute AI ticks to our delegates.
     */
    protected class TickAIDelegateOp implements DelegateOp
    {
        public void apply (PlaceManagerDelegate delegate) {
            ((GameManagerDelegate) delegate).tickAI(_pidx, _level);
        }

        public void setAI (int pidx, byte level)
        {
            _pidx = pidx;
            _level = level;
        }

        protected int _pidx;
        protected byte _level;
    }

    /**
     * Used to determine if this game is a party game.
     */
    protected boolean isPartyGame ()
    {
        return ((_gameconfig instanceof PartyGameConfig) &&
                ((PartyGameConfig)_gameconfig).isPartyGame());
    }

    /** A reference to our game config. */
    protected GameConfig _gameconfig;

    /** A reference to our game object. */
    protected GameObject _gameobj;

    /** The number of players in the game. */
    protected int _playerCount;

    /** The oids of our player and AI body objects. */
    protected int[] _playerOids;

    /** If AIs are present, contains their skill levels, or -1 at human
     * player indexes. */
    protected byte[] _AIs;

    /** If non-null, contains bundles and messages that should be sent as
     * system messages once the game has started. */
    protected ArrayList _startmsgs;

    /** Our delegate operator to tick AIs. */
    protected TickAIDelegateOp _tickAIOp;

    /** A list of all currently active game managers. */
    protected static ArrayList _managers = new ArrayList();

    /** The interval id for the game manager tick interval. */
    protected static int _tiid = -1;

    /** We give players 30 seconds to turn up in a puzzle and after that,
     * they're considered a no show. */
    protected static final long NOSHOW_DELAY = 30 * 1000L;

    /** The delay in milliseconds between ticking of all game managers. */
    protected static final long TICK_DELAY = 5L * 1000L;
}
