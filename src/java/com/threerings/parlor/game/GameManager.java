//
// $Id: GameManager.java,v 1.31 2002/06/01 23:14:53 ray Exp $

package com.threerings.parlor.game;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManagerDelegate;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.ParlorCodes;

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
    implements ParlorCodes, GameCodes, AttributeChangeListener
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return GameObject.class;
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // cast our configuration object (do we need to do this?)
        _gconfig = (GameConfig)_config;

        // register our message handlers
        MessageHandler handler = new PlayerReadyHandler();
        registerMessageHandler(PLAYER_READY_NOTIFICATION, handler);
    }

    /**
     * Initializes the game manager with the supplied game configuration
     * object. This happens before startup and before the game object has
     * been created.
     *
     * @param players the usernames of all of the players in this game or
     * null if the game has no specific set of players.
     */
    public void setPlayers (String[] players)
    {
        // keep this around for now, we'll need it later
        _players = players;

        // instantiate a player oid array which we'll fill in later
        _playerOids = new int[players.length];
    }

    /**
     * Sets the specified player as an AI with the specified skill.
     * It is assumed that this will be set soon after the player names for all
     * AIs present in the game. (It should be done before human players start
     * trickling into the game.)
     *
     * @param pidx the player index of the AI.
     * @param skill the skill level, from 0 to 100 inclusive.
     */
    public void setAI (int pidx, byte skill)
    {
        if (_AIs == null) {
            _AIs = new byte[_players.length];
            for (int ii=0; ii < _players.length; ii++) {
                _AIs[ii] = -1;
            }

            // set up a delegate op for AI ticking
            _tickAIOp = new TickAIDelegateOp();
        }

        _AIs[pidx] = skill;
    }

    /**
     * Returns an array of the usernames of the players in this game.
     */
    public String[] getPlayers ()
    {
        return _players;
    }

    /**
     * Returns whether the game is over.
     */
    public boolean isGameOver ()
    {
        return (_gameobj.state == GameObject.GAME_OVER);
    }

    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        // obtain a casted reference to our game object
        _gameobj = (GameObject)_plobj;

        // stick the players into the game object
        _gameobj.setPlayers(_players);

        // let the players of this game know that we're ready to roll (if
        // we have a specific set of players)
        if (_players != null) {
            Object[] args = new Object[] {
                new Integer(_gameobj.getOid()) };
            for (int i = 0; i < _players.length; i++) {
                BodyObject bobj = CrowdServer.lookupBody(_players[i]);
                if (bobj == null) {
                    Log.warning("Unable to deliver game ready to " +
                                "non-existent player " +
                                "[gameOid=" + _gameobj.getOid() +
                                ", player=" + _players[i] + "].");
                    continue;
                }

                // deliver a game ready notification to the player
                CrowdServer.invmgr.sendNotification(
                    bobj.getOid(), MODULE_NAME, GAME_READY_NOTIFICATION, args);
            }
        }
    }

    // documentation inherited
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // deal with disappearing players
    }

    /**
     * When a game room becomes empty, we cancel the game if it's still in
     * progress and close down the game room.
     */
    protected void placeBecameEmpty ()
    {
        Log.info("Game room empty. Going away. " +
                 "[gameOid=" + _gameobj.getOid() + "].");

        // cancel the game if it wasn't over.
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
        // start up the game (if we haven't already)
        if (_gameobj.state == GameObject.AWAITING_PLAYERS) {
            startGame();
        }
    }

    /**
     * This is called when the game is ready to start (all players
     * involved have delivered their "am ready" notifications). It calls
     * {@link #gameWillStart}, sets the necessary wheels in motion and
     * then calls {@link #gameDidStart}.  Derived classes should override
     * one or both of the calldown functions (rather than this function)
     * if they need to do things before or after the game starts.
     */
    public void startGame ()
    {
        // complain if we're already started
        if (_gameobj.state == GameObject.IN_PLAY) {
            Log.warning("Requested to start an already in-play game " +
                        "[game=" + _gameobj + "].");
            return;
        }

        // let the derived class do its pre-start stuff
        gameWillStart();

        // transition the game to started
        _gameobj.setState(GameObject.IN_PLAY);

        // do post-start processing
        gameDidStart();
    }

    /**
     * Called when the game is about to start, but before the game start
     * notification has been delivered to the players. Derived classes
     * should override this if they need to perform some pre-start
     * activities.
     */
    protected void gameWillStart ()
    {
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
     * transitioning the game to <code>IN_PLAY</code> is necessary).
     */
    protected void gameDidStart ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidStart();
            }
        });

        // and register ourselves to receive AI ticks
        if (_AIs != null) {
            AIGameTicker.registerAIGame(this);
        }
    }

    /**
     * Called by the AIGameTicker if we're registered as an AI game.
     */
    protected void tickAIs ()
    {
        for (int ii=0; ii < _AIs.length; ii++) {
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
     * transition the game to the <code>GAME_OVER</code> state.
     */
    public void endGame ()
    {
        // figure out who won...

        // transition to the game over state
        _gameobj.setState(GameObject.GAME_OVER);

        // wait until we hear the game state transition on the game object
        // to invoke our game over code so that we can be sure that any
        // final events dispatched on the game object prior to the call to
        // endGame() have been dispatched
    }

    /**
     * Called after the game has transitioned to the
     * <code>GAME_OVER</code> state. Derived classes should override this
     * to perform any post-game activities.
     */
    protected void gameDidEnd ()
    {
        // remove ourselves from the AIticker, if applicable
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
     * game. It calls {@link #gameWillReset} and {@link #gameDidReset}.
     * The standard game start processing ({@link #gameWillStart} and
     * {@link #gameDidStart}) will also be called (in between the calls to
     * will and did reset). Derived classes should override one or both of
     * the calldown functions (rather than this function) if they need to
     * do things before or after the game resets.
     */
    public void resetGame ()
    {
        // let the derived class do its pre-reset stuff
        gameWillReset();

        // do the standard game start processing
        gameWillStart();
        _gameobj.setState(GameObject.IN_PLAY);
        gameDidStart();

        // let the derived class do its post-reset stuff
        gameDidReset();
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

    /**
     * Called after the game has been reset.  Derived classes can override
     * this to put whatever wheels they might need into motion now that
     * the game is reset.
     */
    protected void gameDidReset ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidReset();
            }
        });
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(GameObject.STATE)) {
            switch (event.getIntValue()) {
            case GameObject.CANCELLED:
                // fall through to GAME_OVER case
            case GameObject.GAME_OVER:
                // now we do our end of game processing
                gameDidEnd();
                break;
            }
        }
    }

    /** Handles player ready notifications. */
    protected class PlayerReadyHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event, PlaceManager pmgr)
        {
            int cloid = event.getSourceOid();
            BodyObject body = (BodyObject)CrowdServer.omgr.getObject(cloid);
            if (body == null) {
                Log.warning("Player sent am ready notification and then " +
                            "disappeared [event=" + event + "].");
                return;
            }

            // make a note of this player's oid and check to see if we're
            // all set at the same time
            boolean allSet = true;
            for (int i = 0; i < _players.length; i++) {
                if (_players[i].equals(body.username)) {
                    _playerOids[i] = body.getOid();
                }
                if ((_playerOids[i] == 0) &&
                    ((_AIs == null) || (_AIs[i] == -1))) {
                    allSet = false;
                }
            }

            // if everyone is now ready to go, make a note of it
            if (allSet) {
                playersAllHere();
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

    /** A reference to our game configuration. */
    protected GameConfig _gconfig;

    /** A reference to our game object. */
    protected GameObject _gameobj;

    /** The usernames of the players of this game. */
    protected String[] _players;

    /** The oids of our player and AI body objects. */
    protected int[] _playerOids;

    /** If AIs are present, contains their skill levels, or -1 at human
     * player indexes.. */
    protected byte[] _AIs;

    /** Our delegate operator to tick AIs. */
    protected TickAIDelegateOp _tickAIOp;
}
