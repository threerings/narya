//
// $Id: ParlorDirector.java,v 1.21 2004/08/27 02:20:12 mdb Exp $
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

package com.threerings.parlor.client;

import java.util.ArrayList;

import com.samskivert.util.HashIntMap;
import com.threerings.util.Name;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * The parlor director manages the client side of the game configuration
 * and matchmaking processes. It is also the entity that is listening for
 * game start notifications which it then dispatches the client entity
 * that will actually create and display the user interface for the game
 * that started.
 */
public class ParlorDirector extends BasicDirector
    implements ParlorCodes, ParlorReceiver
{
    /**
     * Constructs a parlor director and provides it with the parlor
     * context that it can use to access the client services that it needs
     * to provide its own services. Only one parlor director should be
     * active in the client at one time and it should be made available
     * via the parlor context.
     *
     * @param ctx the parlor context in use by the client.
     */
    public ParlorDirector (ParlorContext ctx)
    {
        super(ctx);
        _ctx = ctx;

        // register ourselves with the invocation director as a parlor
        // notification receiver
        _ctx.getClient().getInvocationDirector().registerReceiver(
            new ParlorDecoder(this));
    }

    /**
     * Sets the invitation handler, which is the entity that will be
     * notified when we receive incoming invitation notifications and when
     * invitations have been cancelled.
     *
     * @param handler our new invitation handler.
     */
    public void setInvitationHandler (InvitationHandler handler)
    {
        _handler = handler;
    }

    /**
     * Adds the specified observer to the list of entities that are
     * notified when we receive a game ready notification.
     */
    public void addGameReadyObserver (GameReadyObserver observer)
    {
        _grobs.add(observer);
    }

    /**
     * Removes the specified observer from the list of entities that are
     * notified when we receive a game ready notification.
     */
    public void removeGameReadyObserver (GameReadyObserver observer)
    {
        _grobs.remove(observer);
    }

    /**
     * Requests that the named user be invited to a game described by the
     * supplied game config.
     *
     * @param invitee the user to invite.
     * @param config the configuration of the game to which the user is
     * being invited.
     * @param observer the entity that will be notified if this invitation
     * is accepted, refused or countered.
     *
     * @return an invitation object that can be used to manage the
     * outstanding invitation.
     */
    public Invitation invite (Name invitee, GameConfig config,
                              InvitationResponseObserver observer)
    {
        // create the invitation record
        Invitation invite = new Invitation(
            _ctx, _pservice, invitee, config, observer);
        // submit the invitation request to the server
        _pservice.invite(_ctx.getClient(), invitee, config, invite);
        // and return the invitation to the caller
        return invite;
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);
        _pservice = null;
        _pendingInvites.clear();
    }

    // documentation inherited
    protected void fetchServices (Client client)
    {
        // get a handle on our parlor services
        _pservice = (ParlorService)client.requireService(ParlorService.class);
    }

    // documentation inherited from interface
    public void gameIsReady (int gameOid)
    {
        Log.info("Handling game ready [goid=" + gameOid + "].");

        // see what our observers have to say about it
        boolean handled = false;
        for (int i = 0; i < _grobs.size(); i++) {
            GameReadyObserver grob = (GameReadyObserver)_grobs.get(i);
            handled = grob.receivedGameReady(gameOid) || handled;
        }

        // if none of the observers took matters into their own hands,
        // then we'll head on over to the game room ourselves
        if (!handled) {
            _ctx.getLocationDirector().moveTo(gameOid);
        }
    }

    // documentation inherited from interface
    public void receivedInvite (int remoteId, Name inviter, GameConfig config)
    {
        // create an invitation record for this invitation
        Invitation invite = new Invitation(
            _ctx, _pservice, inviter, config, null);
        invite.inviteId = remoteId;

        // put it in the pending invitations table
        _pendingInvites.put(remoteId, invite);

        try {
            // notify the invitation handler of the incoming invitation
            _handler.invitationReceived(invite);

        } catch (Exception e) {
            Log.warning("Invitation handler choked on invite " +
                        "notification " + invite + ".");
            Log.logStackTrace(e);
        }
    }

    // documentation inherited from interface
    public void receivedInviteResponse (
        int remoteId, int code, Object arg)
    {
        // look up the invitation record for this invitation
        Invitation invite = (Invitation)_pendingInvites.get(remoteId);
        if (invite == null) {
            Log.warning("Have no record of invitation for which we " +
                        "received a response?! [remoteId=" + remoteId +
                        ", code=" + code + ", arg=" + arg + "].");

        } else {
            invite.receivedResponse(code, arg);
        }
    }

    // documentation inherited from interface
    public void receivedInviteCancellation (int remoteId)
    {
        // TBD
    }

    /**
     * Register a new invitation in our pending invitations table. The
     * invitation will call this when it knows its invitation id.
     */
    protected void registerInvitation (Invitation invite)
    {
        _pendingInvites.put(invite.inviteId, invite);
    }

    /**
     * Called by an invitation when it knows it is no longer and can be
     * cleared from the pending invitations table.
     */
    protected void clearInvitation (Invitation invite)
    {
        _pendingInvites.remove(invite.inviteId);
    }

    /** An active parlor context. */
    protected ParlorContext _ctx;

    /** Provides access to parlor server side services. */
    protected ParlorService _pservice;

    /** The entity that has registered itself to handle incoming
     * invitation notifications. */
    protected InvitationHandler _handler;

    /** A table of acknowledged (but not yet accepted or refused)
     * invitation requests, keyed on invitation id. */
    protected HashIntMap _pendingInvites = new HashIntMap();

    /** We notify the entities on this list when we get a game ready
     * notification. */
    protected ArrayList _grobs = new ArrayList();
}
