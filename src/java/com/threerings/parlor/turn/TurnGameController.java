//
// $Id: TurnGameController.java,v 1.4 2002/02/13 03:21:28 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.parlor.game.GameController;

/**
 * Games that wish to make use of the turn game services should have their
 * controller implement this interface and create an instance of {@link
 * TurnGameControllerDelegate} which should be passed to {@link
 * GameController#addDelegate}.
 */
public interface TurnGameController
{
    /**
     * Called when the turn changed. This indicates the start of a turn
     * and the user interface should adjust itself accordingly (activating
     * controls if it is our turn and deactivating them if it is not).
     *
     * @param turnHolder the username of the new holder of the turn.
     */
    public void turnDidChange (String turnHolder);
}
