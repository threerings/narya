//
// $Id: TurnGameManager.java,v 1.5 2002/02/12 06:57:30 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.parlor.game.GameManager;

/**
 * A game manager that wishes to make use of the turn game services should
 * implement this interface and create a {@link TurnGameManagerDelegate}
 * which will perform the basic turn game processing and call back to the
 * main manager via this interface.
 *
 * <p> The basic flow of a turn-based game is as follows:
 * <pre>
 * GameManager.gameWillStart()
 * GameManager.gameDidStart()
 *   TurnGameManagerDelegate.setFirstTurnHolder()
 *   TurnGameManagerDelegate.startTurn()
 *     TurnGameManager.turnWillStart()
 *   TurnGameManagerDelegate.endTurn()
 *     TurnGameManager.turnDidEnd()
 *   TurnGameManagerDelegate.setNextTurnHolder()
 *   TurnGameManagerDelegate.startTurn()
 *     ...
 * GameManager.endGame()
 * </pre>
 */
public interface TurnGameManager
{
    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public String[] getPlayers ();

    /**
     * Called when we are about to start the next turn. Implementations
     * can do whatever pre-turn activities need to be done.
     */
    public void turnWillStart ();

    /**
     * Called when the turn was ended. Implementations can perform any
     * post-turn processing (like updating scores, etc.).
     */
    public void turnDidEnd ();
}
