//
// $Id: ParlorManager.java,v 1.4 2001/10/02 21:52:33 mdb Exp $

package com.threerings.parlor.server;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

import com.threerings.cocktail.cher.server.InvocationManager;
import com.threerings.cocktail.cher.server.ServiceFailedException;

import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.parlor.Log;
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

        // keep this around for later
        _invmgr = invmgr;
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
     * @return the invitation identifier for the newly created invitation
     * record.
     *
     * @exception ServiceFailedException thrown if the invitation was not
     * able to be processed for some reason (like the invited player has
     * requested not to be disturbed). The explanation will be provided in
     * the message data of the exception.
     */
    public int invite (BodyObject inviter, BodyObject invitee,
                       GameConfig config)
        throws ServiceFailedException
    {
        // here we should check to make sure the invitee hasn't muted the
        // inviter, and that the inviter isn't shunned and all that other
        // access control type stuff

        // create a new invitation record for this invitation
        Invitation invite = new Invitation(inviter, invitee, config);

        // stick it in the pending invites table
        _invites.put(invite.inviteId, invite);

        // deliver an invite notification to the invitee
        Object[] args = new Object[] {
            new Integer(invite.inviteId), inviter.username, config };
        _invmgr.sendNotification(
            invitee.getOid(), MODULE_NAME, INVITE_ID, args);

        // and let the caller know the invite id we assigned
        return invite.inviteId;
    }

    /**
     * Effects a response to an invitation (accept, refuse or counter),
     * made by the specified source user with the specified arguments.
     *
     * @param source the body object of the user that is issuing this
     * response.
     * @param inviteId the identifier of the invitation to which we are
     * responding.
     * @param code the response code (either {@link
     * #INVITATION_ACCEPTED}, {@link #INVITATION_REFUSED} or {@link
     * #INVITATION_COUNTERED}).
     * @param arg the argument that goes along with the response: an
     * explanatory message in the case of a refusal (the empty string, not
     * null, if no message was provided) or the new game configuration in
     * the case of a counter-invitation.
     */
    public void respondToInvite (BodyObject source, int inviteId, int code,
                                 Object arg)
    {
        // look up the invitation
        Invitation invite = (Invitation)_invites.get(inviteId);
        if (invite == null) {
            Log.warning("Requested to respond to non-existent invitation " +
                        "[source=" + source + ", inviteId=" + inviteId +
                        ", code=" + code + ", arg=" + arg + "].");
            return;
        }

        // make sure this response came from the proper person
        if (source != invite.invitee) {
            Log.warning("Got response from non-invitee [source=" + source +
                        ", invite=" + invite + ", code=" + code +
                        ", arg=" + arg + "].");
            return;
        }

        // let the other user know that a response was made to this
        // invitation
        Object[] args = new Object[] {
            new Integer(invite.inviteId), new Integer(code), arg };
        _invmgr.sendNotification(
            invite.inviter.getOid(), MODULE_NAME, RESPOND_INVITE_ID, args);

        switch (code) {
        case INVITATION_ACCEPTED:
            // the invitation was accepted, so we'll need to start up the
            // game and get the necessary balls rolling
            processAcceptedInvitation(invite);
            // and remove the invitation from the pending table
            _invites.remove(inviteId);
            break;

        case INVITATION_REFUSED:
            // remove the invitation record from the pending table as it
            // is no longer pending
            _invites.remove(inviteId);
            break;

        case INVITATION_COUNTERED:
            // swap control of the invitation to the invitee
            invite.swapControl();
            break;

        default:
            Log.warning("Requested to respond to invitation with " +
                        "unknown response code [source=" + source +
                        ", invite=" + invite + ", code=" + code +
                        ", arg=" + arg + "].");
            break;
        }
    }

    public void cancelInvite (BodyObject source, int inviteId)
    {
    }

    protected void processAcceptedInvitation (Invitation invite)
    {
        try {
            // create the game manager and begin it's initialization
            // process. the game manager will take care of notifying the
            // players that the game has been created
            Class gmclass =
                Class.forName(invite.config.getManagerClassName());
            PartyServer.plreg.createPlace(gmclass);

        } catch (Exception e) {
            Log.warning("Unable to create game manager [invite=" + invite +
                        ", error=" + e + "].");
        }
    }

    /**
     * The invitation record is used by the parlor manager to keep track
     * of pending invitations.
     */
    protected static class Invitation
    {
        /** The unique identifier for this inviation. */
        public int inviteId = _nextInviteId++;

        /** The person proposing the invitation. */
        public BodyObject inviter;

        /** The person to whom the invitation is proposed. */
        public BodyObject invitee;

        /** The configuration of the game being proposed. */
        public GameConfig config;

        /**
         * Constructs a new invitation with the specified participants and
         * configuration.
         */
        public Invitation (BodyObject inviter, BodyObject invitee,
                           GameConfig config)
        {
            this.inviter = inviter;
            this.invitee = invitee;
            this.config = config;
        }

        /**
         * Swaps the inviter and invitee which is necessary when the
         * invitee responds with a counter-invitation.
         */
        public void swapControl ()
        {
            BodyObject tmp = inviter;
            inviter = invitee;
            invitee = tmp;
        }
    }

    /** A reference to the invocation manager in operation on this server. */
    protected InvocationManager _invmgr;

    /** The table of pending invitations. */
    protected HashIntMap _invites = new HashIntMap();

    /** A counter used to generate unique identifiers for invitation
     * records. */
    protected static int _nextInviteId = 0;
}
