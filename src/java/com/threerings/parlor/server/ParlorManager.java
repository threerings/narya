//
// $Id: ParlorManager.java,v 1.2 2001/10/01 22:17:34 mdb Exp $

package com.threerings.parlor.server;

import com.samskivert.util.Config;

import com.threerings.cocktail.cher.server.InvocationManager;
import com.threerings.cocktail.party.data.BodyObject;

import com.threerings.parlor.client.ParlorCodes;
import com.threerings.parlor.data.GameConfig;

/**
 * The parlor manager is responsible for the parlor services in
 * aggregate. This includes maintaining the registry of active games,
 * handling the necessary coordination for the matchmaking services and
 * anything else that falls outside the scope of an actual in-progress
 * game.
 */
public class ParlorManager
    implements ParlorCodes
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
        invmgr.registerProvider(MODULE_NAME, pprov);
    }

    /**
     * Issues an invitation from the <code>inviter</code> to the
     * <code>invitee</code> for a game as described by the supplied config
     * object.
     *
     * @param inviter the player initiating the invitation.
     * @param invitee the player being invited.
     * @param config the configuration of the game being proposed.
     *
     * @return the <code>SUCCESS</code> constant if the invitation was
     * accepted and delivered, or a string describing the reason for
     * failure if it was rejected.
     */
    public String invite (BodyObject inviter, BodyObject invitee,
                          GameConfig config)
    {
        return SUCCESS;
    }
}
