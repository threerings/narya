//
// $Id: GameCodes.java,v 1.3 2001/10/11 21:08:21 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.presents.client.InvocationCodes;

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
