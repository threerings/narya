//
// $Id: GameProvider.java,v 1.1 2002/08/14 19:07:53 mdb Exp $

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
}
