//
// $Id: GameManager.java,v 1.14 2001/10/24 01:00:54 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.ParlorCodes;

/**
 * The game manager handles the server side management of a game. It
 * manipulates the game state in accordance with the logic of the game
 * flow and generally manages the whole game playing process.
 *
 * <p> The game manager extends the place manager because games are
 * implicitly played in a location, the players of the game implicitly
 * bodies in that location.
 */
public class GameManager
    extends PlaceManager implements ParlorCodes, GameCodes
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
        Log.info("Game room empty. Going away. [game=" + _gameobj + "].");

        // shut down the place (which will destroy the game object and
        // clean up after everything)
        shutdown();
    }

    /**
     * This is called when the game is ready to start (all players
     * involved have delivered their "am ready" notifications). It calls
     * {@link #gameWillStart}, sets the necessary wheels in motion and
     * then calls {@link #gameDidStart}.  Derived classes should override
     * one or both of the calldown functions (rather than this function)
     * if they need to do things before or after the game starts.
     */
    protected void startGame ()
    {
        // let the derived class do its pre-start stuff
        gameWillStart();

        // transition the game to started
        _gameobj.setState(GameObject.IN_PLAY);

        // and let the derived class do its post-start stuff
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
    }

    /**
     * Called right after the game start notification was delivered.
     * Derived classes should override this to perform whatever
     * machinations are necessary to start the game in motion (if anything
     * other than issuing the game start notification is necessary).
     */
    protected void gameDidStart ()
    {
    }

    /**
     * Called when the game is known to be over. This will call some
     * calldown functions to determine the winner of the game and then
     * transition the game to the <code>GAME_OVER</code> state.
     */
    protected void endGame ()
    {
        // figure out who won...

        // transition to the game over state
        _gameobj.setState(GameObject.GAME_OVER);

        // do our post-game stuff
        gameDidEnd();
    }

    /**
     * Called after the game has transitioned to the
     * <code>GAME_OVER</code> state. Derived classes should override this
     * to perform any post-game activities.
     */
    protected void gameDidEnd ()
    {
        // calculate ratings and all that...
    }

    /** Handles player ready notifications. */
    protected class PlayerReadyHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            int cloid = event.getSourceOid();
            BodyObject body = (BodyObject)
                CrowdServer.omgr.getObject(cloid);
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
                if (_playerOids[i] == 0) {
                    allSet = false;
                }
            }

            // if everyone is now ready to go, start up the game (if we
            // haven't already)
            if (allSet && _gameobj.state == GameObject.AWAITING_PLAYERS) {
                startGame();
            }
        }
    }

    /** A reference to our game configuration. */
    protected GameConfig _gconfig;

    /** A reference to our game object. */
    protected GameObject _gameobj;

    /** The usernames of the players of this game. */
    protected String[] _players;

    /** The oids of our player's body objects. */
    protected int[] _playerOids;
}
