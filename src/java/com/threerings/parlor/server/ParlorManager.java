//
// $Id: ParlorManager.java,v 1.1 2001/10/01 02:56:35 mdb Exp $

package com.threerings.parlor.server;

import com.samskivert.util.Config;

import com.threerings.cocktail.cher.server.InvocationManager;
import com.threerings.parlor.client.ParlorService;

/**
 * The parlor manager is responsible for the parlor services in
 * aggregate. This includes maintaining the registry of active games,
 * handling the necessary coordination for the matchmaking services and
 * anything else that falls outside the scope of an actual in-progress
 * game.
 */
public class ParlorManager
{
    /**
     * Initializes the parlor manager. This should be called by the server
     * that is making use of the parlor services on the single instance of
     * parlor manager that it has created.
     *
     * @param config the configuration object in use by this server.
     * @param invmgr a reference to the invocation manager in use by this
     * server.
     */
    public void init (Config config, InvocationManager invmgr)
    {
        // register our invocation provider
        ParlorProvider pprov = new ParlorProvider(this);
        invmgr.registerProvider(ParlorService.MODULE, pprov);
    }
}
