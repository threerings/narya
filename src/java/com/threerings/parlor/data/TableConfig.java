//
// $Id: TableConfig.java,v 1.1 2001/10/19 02:04:29 mdb Exp $

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
     * Returns the number of players desired for this game, or -1 if the
     * table services should allow up to the maximum number of players to
     * join the table, but also allow the game to be started by the table
     * creator any time after the minimum number of players has arrived.
     */
    public int getDesiredPlayers ();
}
