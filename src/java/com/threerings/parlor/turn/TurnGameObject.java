//
// $Id: TurnGameObject.java,v 1.6 2004/03/06 11:29:19 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.util.Name;

/**
 * Games that wish to support turn-based play must implement this
 * interface with their {@link GameObject}.
 */
public interface TurnGameObject
{
    /**
     * Returns the distributed object field name of the
     * <code>turnHolder</code> field in the object that implements this
     * interface.
     */
    public String getTurnHolderFieldName ();

    /**
     * Returns the username of the player who is currently taking their
     * turn in this turn-based game or <code>null</code> if no user
     * currently holds the turn.
     */
    public Name getTurnHolder ();

    /**
     * Requests that the <code>turnHolder</code> field be set to the specified
     * value.
     */
    public void setTurnHolder (Name turnHolder);

    /**
     * Returns the array of player names involved in the game.
     */
    public Name[] getPlayers ();
}
