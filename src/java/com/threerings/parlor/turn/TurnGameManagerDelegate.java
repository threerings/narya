//
// $Id: TurnGameManagerDelegate.java,v 1.4 2002/02/13 03:21:28 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameManager;
import com.threerings.parlor.game.GameManagerDelegate;
import com.threerings.parlor.util.MathUtil;

/**
 * Performs the server-side turn-based game processing for a turn based
 * game. Game managers which wish to make use of the turn services must
 * implement {@link TurnGameManager} either create an instance of this
 * class, or an instance of a derivation which customizes the behavior,
 * either of which would be passed to {@link GameManager#addDelegate} to
 * be activated.
 */
public class TurnGameManagerDelegate extends GameManagerDelegate
{
    /**
     * Constructs a delegate that will manage the turn game state and call
     * back to the supplied {@link TurnGameManager} implementation to let
     * it in on the progression of the game.
     */
    public TurnGameManagerDelegate (TurnGameManager tgmgr)
    {
        super((GameManager)tgmgr);
        _tgmgr = tgmgr;
    }

    /**
     * Returns the index of the current turn holder as configured in the
     * game object.
     *
     * @return the index into the players array of the current turn holder
     * or -1 if there is no current turn holder.
     */
    public int getTurnHolderIndex ()
    {
        String holder = _turnGame.getTurnHolder();
        for (int i = 0; i < _players.length; i++) {
            if (_players[i].equals(holder)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Called to start the next turn. It calls {@link
     * TurnGameManager#turnWillStart} to allow our owning manager to
     * perform any pre-turn processing and then sets the turn holder that
     * was configured either when the game started or when finishing up
     * the last turn. This assumes that a valid turn holder has been
     * assigned. If some pre-game preparation needs take place in a
     * non-turn-based manner, this function should not be called until it
     * is time to start the first turn.
     */
    public void startTurn ()
    {
        // sanity check
        if (_turnIdx < 0 || _turnIdx >= _players.length) {
            Log.warning("startTurn() called with invalid turn index " +
                        "[turnIdx=" + _turnIdx + "].");
            // abort, abort
            return;
        }

        // let the derived class do their thing
        _tgmgr.turnWillStart();

        // and set the turn indicator accordingly
        _turnGame.setTurnHolder(_players[_turnIdx]);
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
     * GameManager#endGame} if the game is over or do whatever else it
     * needs to do outside the context of the turn flow.  To start things
     * back up again it would set {@link #_turnIdx} to the next turn
     * holder and call {@link #startTurn} itself.
     */
    public void endTurn ()
    {
        // let the manager know that the turn is over
        _tgmgr.turnDidEnd();

        // figure out whose up next
        setNextTurnHolder();

        // and start the next turn if desired
        if (_turnIdx != -1) {
            startTurn();
        }
    }

    // documentation inherited
    public void didStartup (PlaceObject plobj)
    {
        _turnGame = (TurnGameObject)plobj;
    }

    /**
     * This should be called from {@link GameManager#gameDidStart} to let
     * the turn delegate perform start of game processing.
     */
    public void gameDidStart ()
    {
        // grab the players array
        _players = _tgmgr.getPlayers();

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

    /** The game manager for which we are delegating. */
    protected TurnGameManager _tgmgr;

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** Our own happy copy of the game manager's players array. I *love*
     * not having fucking multiple inheritance. */
    protected String[] _players;

    /** The offset into the _players array of the current turn holder or
     * -1 if it's no one's turn. */
    protected int _turnIdx = -1;
}
