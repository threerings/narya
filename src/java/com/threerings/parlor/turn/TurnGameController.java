//
// $Id: TurnGameController.java,v 1.1 2001/10/12 00:30:10 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.presents.dobj.*;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameController;

/**
 * Extends the basic game controller with support for turn-based games.
 */
public abstract class TurnGameController extends GameController
{
    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        super.attributeChanged(event);

        // handle turn changes
        if (event.getName().equals(TurnGameObject.TURN_HOLDER)) {
            turnDidChange((String)event.getValue());
        }
    }

    /**
     * Called when the turn changed. This indicates the start of a turn
     * and the user interface should adjust itself accordingly (activating
     * controls if it is our turn and deactivating them if it is not).
     *
     * @param turnHolder the username of the new holder of the turn.
     */
    protected void turnDidChange (String turnHolder)
    {
        Log.info("Turn changed [holder=" + turnHolder + "].");
    }
}
