//
// $Id: TurnGameObject.java,v 1.1 2002/02/08 23:55:25 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.parlor.game.GameObject;

/**
 * Extends the basic game object with support for turn-based games.
 */
public class TurnGameObject extends GameObject
{
    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The username of the player who is currently taking their turn in
     * this turn-based game or null if no user currently holds the
     * turn. */
    public String turnHolder;

    /**
     * Requests that the <code>turnHolder</code> field be set to the specified
     * value.
     */
    public void setTurnHolder (String turnHolder)
    {
        requestAttributeChange(TURN_HOLDER, turnHolder);
    }

    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value and immediately updates the state of the object
     * to reflect the change. This should <em>only</em> be called on the
     * server and only then if you know what you're doing.
     */
    public void setTurnHolderImmediate (String turnHolder)
    {
        this.turnHolder = turnHolder;
        requestAttributeChange(TURN_HOLDER, turnHolder);
    }
}
