//
// $Id: TurnGameController.java,v 1.6 2004/04/09 17:36:47 ray Exp $

package com.threerings.parlor.turn;

import com.threerings.util.Name;

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
    public void turnDidChange (Name turnHolder);
}
