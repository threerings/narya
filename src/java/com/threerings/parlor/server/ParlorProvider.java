//
// $Id: ParlorProvider.java,v 1.11 2002/04/15 16:28:02 shaper Exp $

package com.threerings.parlor.server;

import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.GameConfig;

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
//          Log.info("Handling invite request [source=" + source +
//                   ", invid=" + invid + ", invitee=" + invitee +
//                   ", config=" + config + "].");

        String rsp = null;

        // ensure that the invitee is online at present
        try {
            BodyObject target = CrowdServer.lookupBody(invitee);
            if (target == null) {
                throw new ServiceFailedException(INVITEE_NOT_ONLINE);
            }

            // submit the invite request to the parlor manager
            int inviteId = _pmgr.invite(source, target, config);
            sendResponse(source, invid, INVITE_RECEIVED_RESPONSE,
                         new Integer(inviteId));

        } catch (ServiceFailedException sfe) {
            // the exception message is the code indicating the reason
            // for the invitation rejection
            sendResponse(source, invid, INVITE_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Processes a request from the client to respond to an outstanding
     * invitation by accepting, refusing, or countering it.
     */
    public void handleRespondInviteRequest (
        BodyObject source, int invid, int inviteId, int code, Object arg)
    {
        // pass this on to the parlor manager
        _pmgr.respondToInvite(source, inviteId, code, arg);
    }

    /**
     * Processes a request from the client to cancel an outstanding
     * invitation.
     */
    public void handleCancelInviteRequest (
        BodyObject source, int invid, int inviteId)
    {
        // pass this on to the parlor manager
        _pmgr.cancelInvite(source, inviteId);
    }

    /**
     * Processes a request from the client to create a new table.
     */
    public void handleCreateTableRequest (
        BodyObject source, int invid, int lobbyOid, GameConfig config)
    {
        Log.info("Handling create table request [source=" + source +
                 ", invid=" + invid + ", lobbyOid=" + lobbyOid +
                 ", config=" + config + "].");

        try {
            // pass the creation request on to the table manager
            TableManager tmgr = getTableManager(lobbyOid);
            int tableId = tmgr.createTable(source, config);
            sendResponse(source, invid, TABLE_CREATED_RESPONSE,
                         new Integer(tableId));

        } catch (ServiceFailedException sfe) {
            // the exception message is the code indicating the reason for
            // the creation rejection
            sendResponse(source, invid, CREATE_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Processes a request from the client to join an existing table.
     */
    public void handleJoinTableRequest (
        BodyObject source, int invid, int lobbyOid, int tableId, int position)
    {
        Log.info("Handling join table request [source=" + source +
                 ", invid=" + invid + ", lobbyOid=" + lobbyOid +
                 ", tableId=" + tableId + ", position=" + position + "].");

        try {
            // pass the join request on to the table manager
            TableManager tmgr = getTableManager(lobbyOid);
            tmgr.joinTable(source, tableId, position);
            // there is normally no response. the client will see
            // themselves show up in the table that they joined

        } catch (ServiceFailedException sfe) {
            // the exception message is the code indicating the reason for
            // the creation rejection
            sendResponse(source, invid, JOIN_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Processes a request from the client to leave an existing table.
     */
    public void handleLeaveTableRequest (
        BodyObject source, int invid, int lobbyOid, int tableId)
    {
        Log.info("Handling leave table request [source=" + source +
                 ", invid=" + invid + ", lobbyOid=" + lobbyOid +
                 ", tableId=" + tableId + "].");

        try {
            // pass the join request on to the table manager
            TableManager tmgr = getTableManager(lobbyOid);
            tmgr.leaveTable(source, tableId);
            // there is normally no response. the client will see
            // themselves removed from the table they just left

        } catch (ServiceFailedException sfe) {
            // the exception message is the code indicating the reason for
            // the creation rejection
            sendResponse(source, invid, LEAVE_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /**
     * Looks up the place manager associated with the supplied lobby oid,
     * casts it to a table lobby manager and obtains the associated table
     * manager reference.
     *
     * @exception ServiceFailedException thrown if something goes wrong
     * along the way like no place manager exists or the place manager
     * that does exist doesn't implement table lobby manager.
     */
    protected TableManager getTableManager (int lobbyOid)
        throws ServiceFailedException
    {
        PlaceManager plmgr = CrowdServer.plreg.getPlaceManager(lobbyOid);
        if (plmgr == null) {
            Log.warning("No place manager exists from which to obtain " +
                        "table manager reference [ploid=" + lobbyOid + "].");
            throw new ServiceFailedException(INTERNAL_ERROR);
        }

        // sanity check
        if (!(plmgr instanceof TableManagerProvider)) {
            Log.warning("Place manager not a table lobby manager " +
                        "[plmgr=" + plmgr + "].");
            throw new ServiceFailedException(INTERNAL_ERROR);
        }

        return ((TableManagerProvider)plmgr).getTableManager();
    }

    /** A reference to the parlor manager we're working with. */
    protected ParlorManager _pmgr;
}
