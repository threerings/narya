//
// $Id: TurnGameObject.java,v 1.4 2002/10/15 23:07:23 shaper Exp $

package com.threerings.parlor.turn;

import com.threerings.parlor.game.GameObject;

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
    public String getTurnHolder ();

    /**
     * Requests that the <code>turnHolder</code> field be set to the specified
     * value.
     */
    public void setTurnHolder (String turnHolder);

    /**
     * Returns the array of player names involved in the game.
     */
    public String[] getPlayers ();
}
