//
// $Id: TableConfig.java,v 1.3 2003/03/27 23:45:04 mdb Exp $

package com.threerings.parlor.data;

/**
 * A game config object that is to be matchmade using the table services
 * should implement this interface so that the table services can extract
 * the necessary table-generic information.
 */
public interface TableConfig
{
    /**
     * Returns the minimum number of players needed to start the game.
     */
    public int getMinimumPlayers ();

    /**
     * Returns the maximum number of players that can play in the game.
     */
    public int getMaximumPlayers ();

    /**
     * Returns the number of players that when reached, will cause the
     * game to automatically be started. If this value is -1 the game will
     * not automatically be started until the maximum number of players is
     * reached.
     */
    public int getDesiredPlayers ();

    /**
     * Sets the desired number of players to the specified value.
     */
    public void setDesiredPlayers (int desiredPlayers);
}
