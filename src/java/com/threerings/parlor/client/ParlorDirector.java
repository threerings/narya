//
// $Id: ParlorDirector.java,v 1.12 2001/10/22 23:56:25 mdb Exp $

package com.threerings.parlor.client;

import java.util.ArrayList;
import java.util.Iterator;
import com.samskivert.util.HashIntMap;

import com.threerings.presents.client.InvocationReceiver;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * The parlor director manages the client side of the game configuration
 * and matchmaking processes. It is also the entity that is listening for
 * game start notifications which it then dispatches the client entity
 * that will actually create and display the user interface for the game
 * that started.
 */
public class ParlorDirector
    implements ParlorCodes, InvocationReceiver
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
        _ctx = ctx;

        // register ourselves with the invocation director as handling
        // parlor notifications
        _ctx.getClient().getInvocationDirector().
            registerReceiver(MODULE_NAME, this);
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
     * Requests that the named user be invited to a game described by the
     * supplied game config.
     *
     * @param invitee the user to invite.
     * @param config the configuration of the game to which the user is
     * being invited.
     * @param observer the entity that will be notified if this invitation
     * is accepted, refused or countered.
     *
     * @return a unique id associated with this invitation that can be
     * used to discern between various outstanding invitations by the
     * invitation observer.
     */
    public int invite (String invitee, GameConfig config,
                       InvitationResponseObserver observer)
    {
        // generate the invocation service request
        int invid = ParlorService.invite(
            _ctx.getClient(), invitee, config, this);

        // create an invitation record and put it in the submitted table
        Invitation invite = new Invitation(invitee, config, observer);
        _submittedInvites.put(invid, invite);
        return invite.inviteId;
    }

    /**
     * Accept an invitation.
     *
     * @param inviteId the id of the invitation to accept.
     */
    public void accept (int inviteId)
    {
        Invitation invite = getInviteByLocalId(inviteId);
        if (invite == null) {
            // complain if we didn't find a matching invitation
            Log.warning("Received request to accept non-existent " +
                        "invitation [inviteId=" + inviteId + "].");
            return;
        }

        // generate the invocation service request
        ParlorService.respond(_ctx.getClient(), invite.remoteId,
                              INVITATION_ACCEPTED, null, this);
    }

    /**
     * Refuse an invitation.
     *
     * @param inviteId the id of the invitation to accept.
     * @param message the message to deliver to the inviting user
     * explaining the reason for the refusal or null if no message is to
     * be provided.
     */
    public void refuse (int inviteId, String message)
    {
        Invitation invite = getInviteByLocalId(inviteId);
        if (invite == null) {
            // complain if we didn't find a matching invitation
            Log.warning("Received request to accept non-existent " +
                        "invitation [inviteId=" + inviteId + "].");
            return;
        }

        // generate the invocation service request
        ParlorService.respond(_ctx.getClient(), invite.remoteId,
                              INVITATION_REFUSED, message, this);
    }

    /**
     * Counters a received invitation with an invitation with different
     * game configuration parameters.
     *
     * @param inviteId the id of the received invitation.
     * @param config the updated game configuration.
     * @param observer the entity that will be notified if this
     * counter-invitation is accepted, refused or countered.
     */
    public void counter (int inviteId, GameConfig config,
                         InvitationResponseObserver observer)
    {
        Invitation invite = getInviteByLocalId(inviteId);
        if (invite == null) {
            // complain if we didn't find a matching invitation
            Log.warning("Received request to counter non-existent " +
                        "invitation [inviteId=" + inviteId +
                        ", config=" + config + "].");
            return;
        }

        // update the invitation record with the observer (who will
        // eventually be hearing back from the other client about their
        // counter-invitation)
        invite.observer = observer;

        // generate the invocation service request
        ParlorService.respond(_ctx.getClient(), invite.remoteId,
                              INVITATION_COUNTERED, config, this);
    }

    /**
     * Issues a request to cancel an outstanding invitation.
     *
     * @param inviteId the id of the invitation to cancel.
     */
    public void cancel (int inviteId)
    {
        // look up the invitation record (oh the two separate key spaces
        // humanity)
        Invitation invite = getInviteByLocalId(inviteId);
        if (invite == null) {
            // complain if we didn't find a matching invitation
            Log.warning("Received request to cancel non-existent " +
                        "invitation [inviteId=" + inviteId + "].");
            return;
        }

        // if the invitation has not yet been acknowleged by the server,
        // we make a note that it should be cancelled when we do receive
        // the acknowlegement
        if (invite.remoteId == -1) {
            invite.cancelled = true;

        } else {
            // otherwise, generate the invocation service request
            ParlorService.cancel(_ctx.getClient(), invite.remoteId, this);
            // and remove it from the pending table
            _pendingInvites.remove(invite.remoteId);
        }
    }

    /**
     * Called by the invocation services when a game in which we are a
     * player is ready to begin.
     *
     * @param gameOid the object id of the game object.
     */
    public void handleGameReadyNotification (int gameOid)
    {
        Log.info("Handling game ready [goid=" + gameOid + "].");

        // go there
        _ctx.getLocationDirector().moveTo(gameOid);
    }

    /**
     * Called by the invocation services when another user has invited us
     * to play a game.
     *
     * @param remoteId the unique indentifier for this invitation (used
     * when countering or responding).
     * @param inviter the username of the inviting user.
     * @param config the configuration information for the game to which
     * we've been invited.
     */
    public void handleInviteNotification (
        int remoteId, String inviter, GameConfig config)
    {
        // create an invitation record for this invitation
        Invitation invite = new Invitation(inviter, config, null);
        invite.remoteId = remoteId;

        // put it in the pending invitations table
        _pendingInvites.put(remoteId, invite);

        try {
            // notify the invitation handler of the incoming invitation
            _handler.invitationReceived(invite.inviteId, inviter, config);

        } catch (Exception e) {
            Log.warning("Invitation handler choked on invite " +
                        "notification [inviteId=" + invite.inviteId +
                        ", inviter=" + inviter +
                        ", config=" + config + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Called by the invocation services when another user has responded
     * to our invitation by either accepting, refusing or countering it.
     *
     * @param remoteId the unique indentifier for the invitation.
     * @param code the response code, either {@link
     * ParlorCodes#INVITATION_ACCEPTED} or {@link
     * ParlorCodes#INVITATION_REFUSED} or {@link
     * ParlorCodes#INVITATION_COUNTERED}.
     * @param arg in the case of a refused invitation, a string
     * containing a message provided by the invited user explaining the
     * reason for refusal (the empty string if no explanation was
     * provided). In the case of a countered invitation, a new game config
     * object with the modified game configuration.
     */
    public void handleRespondInviteNotification (
        int remoteId, int code, Object arg)
    {
        // look up the invitation record for this invitation
        Invitation invite = (Invitation)_pendingInvites.get(remoteId);
        if (invite == null) {
            Log.warning("Have no record of invitation for which we " +
                        "received a response?! [remoteId=" + remoteId +
                        ", code=" + code + ", arg=" + arg + "].");
            return;
        }

        // make sure we have an observer to notify
        if (invite.observer == null) {
            Log.warning("No observer registered for invitation " +
                        invite + ".");
            return;
        }

        // notify the observer
        try {
            switch (code) {
            case INVITATION_ACCEPTED:
                invite.observer.invitationAccepted(invite.inviteId);
                break;

            case INVITATION_REFUSED:
                invite.observer.invitationRefused(
                    invite.inviteId, (String)arg);
                break;

            case INVITATION_COUNTERED:
                invite.observer.invitationCountered(
                    invite.inviteId, (GameConfig)arg);
                break;
            }

        } catch (Exception e) {
            Log.warning("Invitation response observer choked on response " +
                        "[code=" + code + ", arg=" + arg +
                        ", invite=" + invite + "].");
            Log.logStackTrace(e);
        }

        // unless the invitation was countered, we can remove it from the
        // pending table because it's resolved
        if (code != INVITATION_COUNTERED) {
            _pendingInvites.remove(remoteId);
        }
    }

    /**
     * Called by the invocation services when an outstanding invitation
     * has been cancelled by the inviting user.
     *
     * @param remoteId the unique indentifier for the invitation.
     */
    public void handleCancelInviteNotification (int remoteId)
    {
        // TBD
    }

    /**
     * Called by the invocation services when an invitation request was
     * received by the server and delivered to the intended invitee.
     *
     * @param invid the invocation id of the invitation request.
     */
    public void handleInviteReceived (int invid, int remoteId)
    {
        // remove the invitation record from the submitted table and put
        // it in the pending table
        Invitation invite = (Invitation)_submittedInvites.remove(invid);
        if (invite == null) {
            Log.warning("Received accepted notification for non-existent " +
                        "invitation request!? [invid=" + invid +
                        ", remoteId=" + remoteId + "].");
            return;
        }

        // now that we know the invitation's unique id, keep track of it
        invite.remoteId = remoteId;

        // if the invitation was cancelled before we heard back about it,
        // we need to send off a cancellation request now
        if (invite.cancelled) {
            // generate the invocation service request to cancel it
            ParlorService.cancel(_ctx.getClient(), invite.remoteId, this);

        } else {
            // otherwise, put it in the new table
            _pendingInvites.put(remoteId, invite);
        }
    }

    /**
     * Called by the invocation services when an invitation request failed
     * or was rejected for some reason (this is different than an
     * invitation being refused by the invitee which is handled by {@link
     * #handleRespondInviteNotification}).
     *
     * @param invid the invocation id of the invitation request.
     * @param reason a reason code explaining the failure.
     */
    public void handleInviteFailed (int invid, String reason)
    {
        Log.info("Handling invite failed [invid=" + invid +
                 ", reason=" + reason + "].");

        // remove the invitation record from the submitted table and let
        // the observer know that we're hosed
        Invitation invite = (Invitation)_submittedInvites.remove(invid);
        if (invite == null) {
            Log.warning("Received failed notification for non-existent " +
                        "invitation request!? [invid=" + invid +
                        ", reason=" + reason + "].");
            return;
        }

        // let the observer know what's up
        try {
            invite.observer.invitationRefused(invite.inviteId, reason);
        } catch (Exception e) {
            Log.warning("Invite observer choked on refusal notification " +
                        "[invite=" + invite + ", reason=" + reason + "].");
            Log.logStackTrace(e);
        }
    }

    public void handleTableCreated (int invid, int tableId)
    {
    }

    public void handleCreateFailed (int invid, String reason)
    {
    }

    public void handleTableJoined (int invid, int tableId)
    {
    }

    public void handleJoinFailed (int invid, String reason)
    {
    }

    /**
     * The invitation class is used to track information related to
     * outstanding invitations generated by or targeted to this client.
     */
    protected static class Invitation
    {
        /** A unique id for this invitation assigned on the client which
         * is only used to allow the invitation observers to discriminate
         * between multiple outstanding invitations. We'd use the remote
         * id here except that that is not known until we receive the
         * acknowlegedment from the server and we need to provide the
         * caller with some sort of unique id at the time the request is
         * generated. */
        public int inviteId = _localInvitationId++;

        /** The unique id for this invitation (as assigned by the
         * server). This is -1 until we receive an acknowledgement from
         * the server that our invitation was delivered. */
        public int remoteId = -1;

        /** The name of the other user involved in this invitation. */
        public String opponent;

        /** The configuration of the game to be created. */
        public GameConfig config;

        /** The entity to notify when we receive a response for this
         * invitation. */
        public InvitationResponseObserver observer;

        /** A flag indicating that we were requested to cancel this
         * invitation before we even heard back with an acknowledgement
         * that it was received by the server. */
        public boolean cancelled = false;

        /** Constructs a new invitation record. */
        public Invitation (String opponent, GameConfig config,
                           InvitationResponseObserver observer)
        {
            this.opponent = opponent;
            this.config = config;
            this.observer = observer;
        }

        /** Returns a string representation of this invitation record. */
        public String toString ()
        {
            return "[inviteId=" + inviteId + ", remoteId=" + remoteId +
                ", opponent=" + opponent + ", config=" + config +
                ", observer=" + observer + ", cancelled=" + cancelled + "]";
        }
    }

    /**
     * Looks up an invitation by its local unique identifier. Oh the dual
     * uid space humanity!
     */
    protected Invitation getInviteByLocalId (int inviteId)
    {
        // first search the pending invites which is where we're most
        // likely to find it
        Iterator iter = _pendingInvites.values().iterator();
        while (iter.hasNext()) {
            Invitation match = (Invitation)iter.next();
            if (match.inviteId == inviteId) {
                return match;
            }
        }

        // if that didn't work, look in the submitted invites
        iter = _submittedInvites.values().iterator();
        while (iter.hasNext()) {
            Invitation match = (Invitation)iter.next();
            if (match.inviteId == inviteId) {
                return match;
            }
        }

        // il n'existe pas
        return null;
    }

    /** An active parlor context. */
    protected ParlorContext _ctx;

    /** The entity that has registered itself to handle incoming
     * invitation notifications. */
    protected InvitationHandler _handler;

    /** A table of submitted (but not acknowledged) invitation requests,
     * keyed on invocation request id. */
    protected HashIntMap _submittedInvites = new HashIntMap();

    /** A table of acknowledged (but not yet accepted or refused)
     * invitation requests, keyed on invitation id. */
    protected HashIntMap _pendingInvites = new HashIntMap();

    /** A counter used to assign a unique id to every invitation. */
    protected static int _localInvitationId = 0;
}
