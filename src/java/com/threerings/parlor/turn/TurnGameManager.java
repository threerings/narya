//
// $Id: TurnGameManager.java,v 1.1 2001/10/12 00:30:10 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameManager;
import com.threerings.parlor.util.MathUtil;

/**
 * Extends the basic game manager with support for turn-based games.
 */
public class TurnGameManager extends GameManager
{
    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        // obtain a casted reference to our turn game object
        _turnGame = (TurnGameObject)_plobj;
    }

    // documentation inherited
    protected void gameDidStart ()
    {
        super.gameDidStart();

        // figure out who will be first
        setFirstTurnHolder();

        // and set the turn indicator accordingly
        int boid = _playerOids[_turnIdx];
        BodyObject body = (BodyObject)CrowdServer.omgr.getObject(boid);
        if (body != null) {
            _turnGame.setTurnHolder(body.username);
        } else {
            Log.warning("Unable to start game; first player isn't around " +
                        "[boid=" + boid + "].");
        }
    }

    /**
     * This is called to determine whichi player will take the first
     * turn. The default implementation chooses a player at random.
     */
    protected void setFirstTurnHolder ()
    {
        // TODO: sort out a better random number generator and make it
        // available via the parlor services
        _turnIdx = MathUtil.random(_playerOids.length);
    }

    /**
     * This is called to determine which player will next hold the turn.
     * The default implementation simply rotates through the players in
     * order, but some games may need to mess with the turn from time to
     * time. This should update the <code>_turnIdx</code> field, not set
     * the turn holder field in the game object directly.
     */
    protected void setNextTurnHolder ()
    {
        // next!
        _turnIdx = (_turnIdx + 1) % _playerOids.length;
    }

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** The offset into the _playerOids array of the current turn holder
     * or -1 if it's no one's turn. */
    protected int _turnIdx = -1;
}
