//
// $Id: GameProvider.java,v 1.2 2002/09/06 22:52:27 shaper Exp $

package com.threerings.parlor.game;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

/**
 * Provides access to the server-side implementation of the game
 * invocation services.
 */
public interface GameProvider extends InvocationProvider
{
    /**
     * Called when the client has sent a {@link GameService#playerReady}
     * service request.
     */
    public void playerReady (ClientObject caller);

    /**
     * Called when the client has sent a {@link
     * GameService#startPartyGame} service request.
     */
    public void startPartyGame (ClientObject caller);
}
