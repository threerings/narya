//
// $Id: ParlorProvider.java,v 1.2 2001/10/01 05:07:13 mdb Exp $

package com.threerings.parlor.server;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.party.data.BodyObject;

import com.threerings.parlor.data.GameConfig;

/**
 * The parlor provider handles the server side of the various Parlor
 * services that are made available for direct invocation by the client.
 * Primarily these are the matchmaking mechanisms.
 */
public class ParlorProvider extends InvocationProvider
{
    /**
     * Constructs a parlor provider instance which will be used to handle
     * all parlor-related invocation service requests. This is
     * automatically taken care of by the parlor manager, so no other
     * entity need instantiate and register a parlor provider.
     *
     * @param pmgr a reference to the parlor manager active in this
     * server.
     */
    public ParlorProvider (ParlorManager pmgr)
    {
        _pmgr = pmgr;
    }

    /**
     * Processes a request from the client to invite another user to play
     * a game.
     */
    public void handleInviteRequest (
        BodyObject source, int invid, String invitee, GameConfig config)
    {
    }

    /**
     * Processes a request from the client to respond to an outstanding
     * invitation by accepting, refusing, or countering it.
     */
    public void handleRepsondInviteRequest (
        BodyObject source, int invid, int inviteId, int code, Object arg)
    {
    }

    /**
     * Processes a request from the client to cancel an outstanding
     * invitation.
     */
    public void handleCancelInviteRequest (
        BodyObject source, int invid, int inviteId)
    {
    }

    /** A reference to the parlor manager we're working with. */
    protected ParlorManager _pmgr;
}
