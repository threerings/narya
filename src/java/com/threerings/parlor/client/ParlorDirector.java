//
// $Id: ParlorDirector.java,v 1.1 2001/10/01 02:56:35 mdb Exp $

package com.threerings.parlor.client;

import com.samskivert.util.HashIntMap;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * The parlor director manages the client side of the game configuration
 * and matchmaking processes. It is also the entity that is listening for
 * game start notifications which it then dispatches the client entity
 * that will actually create and display the user interface for the game
 * that started.
 */
public class ParlorDirector
{
    /**
     * Requests that the named user be invited to a game described by the
     * supplied game config.
     *
     * @param invitee the user to invite.
     * @param config the configuration of the game to which the user is
     * being invited.
     * @param observer the entity that wants to know about accepted,
     * rejected or countered invitations.
     *
     * @return a unique id associated with this invitation that can be
     * used to discern between various outstanding invitations by the
     * invitation observer.
     */
    public int invite (ParlorContext ctx, String invitee,
                       GameConfig config, InvitationObserver observer)
    {
        // generate the invocation service request
        int invid = ParlorService.invite(
            ctx.getClient(), invitee, config, this);

        // create an invitation record and put it in the submitted table
        Invitation invite = new Invitation(invitee, config, observer);
        _submittedInvites.put(invid, invite);
        return invite.localId;
    }

    public void counter (ParlorContext ctx, int inviteId,
                         GameConfig config, InvitationObserver observer)
    {
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
    }

    /**
     * Called by the invocation services when another user has responded
     * to our invitation.
     *
     * @param remoteId the unique indentifier for the invitation.
     * @param code the response code {@link
     * ParlorService#INVITATION_ACCEPTED} or {@link
     * ParlorService#INVITATION_REJECTED}.
     * @param message in the case of a rejected invitation, a message
     * provided by the invited user explaining the rejection. The empty
     * string if no explanation was provided.
     */
    public void handleResponseNotification (
        int remoteId, int code, String message)
    {
    }

    /**
     * Called by the invocation services when another user has invited us
     * to play a game.
     *
     * @param inviter the username of the inviting user.
     * @param config the configuration information for the game to which
     * we've been invited.
     */
    public void handleInviteNotification (String inviter, GameConfig config)
    {
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
        Invitation invite = (Invitation)_submittedInvites.get(invid);
        if (invite == null) {
            Log.warning("Received accepted notification for non-existent " +
                        "invitation request!? [invid=" + invid +
                        ", remoteId=" + remoteId + "].");
            return;
        }

        // now that we know the invitation's unique id, keep track of it
        invite.remoteId = remoteId;

        // and put it in the new table
        _pendingInvites.put(remoteId, invite);
    }

    /**
     * Called by the invocation services when an invitation request failed
     * or was rejected for some reason.
     *
     * @param invid the invocation id of the invitation request.
     * @param reason a reason code explaining the rejection or failure.
     */
    public void handleInviteFailed (int invid, String reason)
    {
    }

    protected static class Invitation
    {
        /** A unique id for this invitation assigned on the client which
         * is only used to allow the invitation observers to discriminate
         * between multiple outstanding invitations. We'd use the remote
         * id here except that that is not known until we receive the
         * acknowlegedment from the server and we need to provide the
         * caller with some sort of unique id at the time the request is
         * generated. */
        public int localId = _localInvitationId++;

        /** The unique id for this invitation (as assigned by the
         * server). This is -1 until we receive an acknowledgement from
         * the server that our invitation was delivered. */
        public int remoteId = -1;

        /** The name of the user that was invited. */
        public String invitee;

        /** The configuration of the game to be created. */
        public GameConfig config;

        /** The invitation observer that created this invitation. */
        public InvitationObserver observer;

        /** A flag indicating that we were requested to abort this
         * invitation before we even heard back with an acknowledgement
         * that it was received by the server. */
        public boolean aborted = false;

        /** Constructs a new invitation request. */
        public Invitation (String invitee, GameConfig config,
                           InvitationObserver observer)
        {
            this.invitee = invitee;
            this.config = config;
            this.observer = observer;
        }
    }

    /** A table of submitted (but not acknowledged) invitation requests,
     * keyed on invocation request id. */
    protected HashIntMap _submittedInvites = new HashIntMap();

    /** A table of acknowledged (but not yet accepted or refused)
     * invitation requests, keyed on invitation id. */
    protected HashIntMap _pendingInvites = new HashIntMap();

    /** A counter used to assign a unique id to every invitation. */
    protected static int _localInvitationId = 0;
}
