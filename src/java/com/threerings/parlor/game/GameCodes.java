//
// $Id: GameCodes.java,v 1.4 2002/04/15 16:28:02 shaper Exp $

package com.threerings.parlor.game;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the game services.
 */
public interface GameCodes extends InvocationCodes
{
    /** The message identifier for a player ready notification. This is
     * delivered by the game controller when the client has loaded the
     * user interface for the game and is ready to play. */
    public static final String PLAYER_READY_NOTIFICATION = "PlayerReady";
}
