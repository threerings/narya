//
// $Id: TurnGameManager.java,v 1.4 2001/10/19 21:07:06 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameManager;
import com.threerings.parlor.util.MathUtil;

/**
 * Extends the basic game manager with support for turn-based games.
 *
 * <p> The basic flow of a turn-based game is as follows:
 * <pre>
 * gameWillStart()
 * gameDidStart()
 *   setFirstTurnHolder()
 *   startTurn()
 * </pre>
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

        // and start the first turn if we should apparently do so
        if (_turnIdx != -1) {
            startTurn();
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
        _turnIdx = MathUtil.random(_players.length);
    }

    /**
     * Called to start the next turn. It calls the derived class to allow
     * it to perform any pre-turn processing and then sets the turn holder
     * that was configured either when the game started or when finishing
     * up the last turn. This assumes that a valid turn holder has been
     * assigned. If some pre-game preparation needs take place in a
     * non-turn-based manner, this function should not be called until it
     * is time to start the first turn.
     */
    protected void startTurn ()
    {
        // sanity check
        if (_turnIdx < 0 || _turnIdx >= _players.length) {
            Log.warning("startTurn() called with invalid turn index " +
                        "[turnIdx=" + _turnIdx + "].");
            // abort, abort
            return;
        }

        // let the derived class do their thing
        turnWillStart();

        // and set the turn indicator accordingly
        _turnGame.setTurnHolder(_players[_turnIdx]);
    }

    /**
     * Called when we are about to start the next turn. Derived classes
     * should override this and do whatever pre-turn activities need to be
     * done.
     */
    protected void turnWillStart ()
    {
    }

    /**
     * Called to end the turn. Whatever indication a game manager has that
     * the turn has ended (probably the submission of a valid move of some
     * sort by the turn holding player), it should call this function to
     * cause this turn to end and the next to begin.
     *
     * <p> If the next turn should not be started immediately after this
     * turn, the game manager should arrange for {@link
     * #setNextTurnHolder} to set the {@link #_turnIdx} field to -1 which
     * will cause us not to start the next turn. It can then call {@link
     * #endGame} if the game is over or do whatever else it needs to do
     * outside the context of the turn flow.  To start things back up
     * again it would set {@link #_turnIdx} to the next turn holder and
     * call {@link #startTurn} itself.
     */
    protected void endTurn ()
    {
        // let the derived class know that the turn is over
        turnDidEnd();

        // figure out whose up next
        setNextTurnHolder();

        // and start the next turn if desired
        if (_turnIdx != -1) {
            startTurn();
        }
    }

    /**
     * Called when the turn was ended. Derived classes should override
     * this and perform any post-turn processing (like updating scores,
     * etc.).
     */
    protected void turnDidEnd ()
    {
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
        _turnIdx = (_turnIdx + 1) % _players.length;
    }

    /**
     * Returns the index of the current turn holder as configured in the
     * game object.
     *
     * @return the index into the players array of the current turn holder
     * or -1 if there is no current turn holder.
     */
    protected int getTurnHolderIndex ()
    {
        for (int i = 0; i < _players.length; i++) {
            if (_players[i].equals(_turnGame.turnHolder)) {
                return i;
            }
        }
        return -1;
    }

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** The offset into the _players array of the current turn holder or
     * -1 if it's no one's turn. */
    protected int _turnIdx = -1;
}
