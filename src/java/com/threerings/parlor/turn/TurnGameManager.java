//
// $Id: TurnGameManager.java,v 1.9 2004/02/25 14:44:54 mdb Exp $

package com.threerings.parlor.turn;

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
    public String getPlayerName (int index);

    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public int getPlayerIndex (String username);

    /**
     * Extending {@link GameManager} should automatically handle
     * implementing this method.
     */
    public int getPlayerCount ();

    /**
     * Called when we are about to start the next turn. Implementations
     * can do whatever pre-start turn activities need to be done.
     */
    public void turnWillStart ();

    /**
     * Called when we have started the next turn. Implementations can do
     * whatever post-start turn activities need to be done.
     */
    public void turnDidStart ();

    /**
     * Called when the turn was ended. Implementations can perform any
     * post-turn processing (like updating scores, etc.).
     */
    public void turnDidEnd ();
}
