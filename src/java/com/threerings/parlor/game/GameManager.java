//
// $Id: GameManager.java,v 1.4 2001/10/06 00:25:29 mdb Exp $

package com.threerings.parlor.server;

import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.server.PlaceManager;
import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.ParlorCodes;
import com.threerings.parlor.data.GameConfig;
import com.threerings.parlor.data.GameObject;

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
    extends PlaceManager implements ParlorCodes
{
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
        // keep this info for later
        _players = players;
    }

    // documentation inherited
    protected void didStartup ()
    {
        // obtain a casted reference to our game object
        _gameobj = (GameObject)_plobj;

        // let the players of this game know that we're ready to roll (if
        // we have a specific set of players)
        if (_players != null) {
            Object[] args = new Object[] {
                new Integer(_gameobj.getOid()) };
            for (int i = 0; i < _players.length; i++) {
                BodyObject bobj = PartyServer.lookupBody(_players[i]);
                // deliver a game ready notification to the player
                PartyServer.invmgr.sendNotification(
                    bobj.getOid(), MODULE_NAME, GAME_READY_NOTIFICATION, args);
            }
        }
    }

    // documentation inherited
    protected void bodyEntered (int bodyOid)
    {
    }

    // documentation inherited
    protected void bodyLeft (int bodyOid)
    {
    }

    /** A reference to our game configuration. */
    protected GameConfig _gconfig;

    /** The usernames of the players of this game. */
    protected String[] _players;

    /** A reference to our game object. */
    protected GameObject _gameobj;
}
