//
// $Id: ParlorProvider.java,v 1.3 2001/10/01 22:17:34 mdb Exp $

package com.threerings.parlor.server;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.parlor.client.ParlorCodes;
import com.threerings.parlor.data.GameConfig;

/**
 * The parlor provider handles the server side of the various Parlor
 * services that are made available for direct invocation by the client.
 * Primarily these are the matchmaking mechanisms.
 */
public class ParlorProvider
    extends InvocationProvider implements ParlorCodes
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
        String rsp = null;

        // ensure that the invitee is online at present
        BodyObject target = PartyServer.lookupBody(invitee);
        if (target == null) {
            rsp = "m.not_online\t" + invitee;

        } else {
            // if they are, submit the invite request to the parlor
            // manager
            rsp = _pmgr.invite(source, target, config);
        }

        // now send the response
        if (rsp.equals(SUCCESS)) {
            sendResponse(source, invid, "InviteReceived");
        } else {
            sendResponse(source, invid, "InviteReceived");
        }
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
