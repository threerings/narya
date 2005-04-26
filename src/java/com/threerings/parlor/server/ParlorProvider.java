//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.parlor.server;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.ParlorService.InviteListener;
import com.threerings.parlor.client.ParlorService.TableListener;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.server.GameManager;

/**
 * The parlor provider handles the server side of the various Parlor
 * services that are made available for direct invocation by the client.
 * Primarily these are the matchmaking mechanisms.
 */
public class ParlorProvider
    implements InvocationProvider, ParlorCodes
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
    public void invite (ClientObject caller, Name invitee,
                        GameConfig config, InviteListener listener)
        throws InvocationException
    {
//          Log.info("Handling invite request [source=" + source +
//                   ", invitee=" + invitee + ", config=" + config + "].");

        BodyObject source = (BodyObject)caller;
        String rsp = null;

        // ensure that the invitee is online at present
        BodyObject target = CrowdServer.lookupBody(invitee);
        if (target == null) {
            throw new InvocationException(INVITEE_NOT_ONLINE);
        }

        // submit the invite request to the parlor manager
        int inviteId = _pmgr.invite(source, target, config);
        listener.inviteReceived(inviteId);
    }

    /**
     * Processes a request from the client to respond to an outstanding
     * invitation by accepting, refusing, or countering it.
     */
    public void respond (ClientObject caller, int inviteId, int code,
                         Object arg, InvocationListener listener)
    {
        // pass this on to the parlor manager
        _pmgr.respondToInvite((BodyObject)caller, inviteId, code, arg);
    }

    /**
     * Processes a request from the client to cancel an outstanding
     * invitation.
     */
    public void cancel (ClientObject caller, int inviteId,
                        InvocationListener listener)
    {
        // pass this on to the parlor manager
        _pmgr.cancelInvite((BodyObject)caller, inviteId);
    }

    /**
     * Processes a request from the client to create a new table.
     */
    public void createTable (ClientObject caller, int lobbyOid,
            TableConfig tableConfig, GameConfig config, TableListener listener)
        throws InvocationException
    {
        Log.info("Handling create table request [caller=" + caller.who() +
                 ", lobbyOid=" + lobbyOid + ", config=" + config + "].");

        // pass the creation request on to the table manager
        TableManager tmgr = getTableManager(lobbyOid);
        int tableId = tmgr.createTable((BodyObject)caller, tableConfig, config);
        listener.tableCreated(tableId);
    }

    /**
     * Processes a request from the client to join an existing table.
     */
    public void joinTable (ClientObject caller, int lobbyOid, int tableId,
                           int position, InvocationListener listener)
        throws InvocationException
    {
        Log.info("Handling join table request [caller=" + caller.who() +
                 ", lobbyOid=" + lobbyOid + ", tableId=" + tableId +
                 ", position=" + position + "].");

        // pass the join request on to the table manager
        TableManager tmgr = getTableManager(lobbyOid);
        tmgr.joinTable((BodyObject)caller, tableId, position);

        // there is normally no success response. the client will see
        // themselves show up in the table that they joined
    }

    /**
     * Processes a request from the client to leave an existing table.
     */
    public void leaveTable (ClientObject caller, int lobbyOid, int tableId,
                            InvocationListener listener)
        throws InvocationException
    {
        Log.info("Handling leave table request [caller=" + caller.who() +
                 ", lobbyOid=" + lobbyOid + ", tableId=" + tableId + "].");

        // pass the join request on to the table manager
        TableManager tmgr = getTableManager(lobbyOid);
        tmgr.leaveTable((BodyObject)caller, tableId);

        // there is normally no success response. the client will see
        // themselves removed from the table they just left
    }

    /**
     * Handles a {@link ParlorService#startSolitaire} request.
     */
    public void startSolitaire (ClientObject caller, GameConfig config,
                                InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject user = (BodyObject)caller;

        Log.debug("Processing start puzzle [caller=" + user.who() +
                  ", config=" + config + "].");

        try {
            // just this fellow will be playing
            config.players = new Name[] { user.username };

            // create the game manager and begin its initialization
            // process
            GameManager gmgr = (GameManager)
                CrowdServer.plreg.createPlace(config, null);

            // the game manager will take care of notifying the player
            // that the game has been created once it has been started up;
            // but we let the caller know that we processed their request
            listener.requestProcessed();

        } catch (InstantiationException ie) {
            Log.warning("Error instantiating game manager " +
                        "[for=" + caller.who() + ", config=" + config + "].");
            Log.logStackTrace(ie);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /**
     * Looks up the place manager associated with the supplied lobby oid,
     * casts it to a table lobby manager and obtains the associated table
     * manager reference.
     *
     * @exception InvocationException thrown if something goes wrong
     * along the way like no place manager exists or the place manager
     * that does exist doesn't implement table lobby manager.
     */
    protected TableManager getTableManager (int lobbyOid)
        throws InvocationException
    {
        PlaceManager plmgr = CrowdServer.plreg.getPlaceManager(lobbyOid);
        if (plmgr == null) {
            Log.warning("No place manager exists from which to obtain " +
                        "table manager reference [ploid=" + lobbyOid + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // sanity check
        if (!(plmgr instanceof TableManagerProvider)) {
            Log.warning("Place manager not a table lobby manager " +
                        "[plmgr=" + plmgr + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        return ((TableManagerProvider)plmgr).getTableManager();
    }

    /** A reference to the parlor manager we're working with. */
    protected ParlorManager _pmgr;
}
