//
// $Id: ParlorManager.java,v 1.11 2001/10/19 02:04:29 mdb Exp $

package com.threerings.parlor.server;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.ParlorCodes;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.game.GameManager;

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
//          Log.info("Received invitation request [inviter=" + inviter +
//                   ", invitee=" + invitee + ", config=" + config + "].");

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
            Log.info("Creating game manager [invite=" + invite + "].");

            // create the game manager and begin it's initialization
            // process. the game manager will take care of notifying the
            // players that the game has been created once it has been
            // started up (which is done by the place registry once the
            // game object creation has completed)
            GameManager gmgr = (GameManager)
                CrowdServer.plreg.createPlace(invite.config);

            // provide the game manager with some initialization info
            String[] players = new String[] {
                invite.invitee.username, invite.inviter.username };
            gmgr.setPlayers(players);

        } catch (Exception e) {
            Log.warning("Unable to create game manager [invite=" + invite +
                        ", error=" + e + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Requests that a new table be created to matchmake the game
     * described by the supplied game config instance. The config instance
     * provided must implement the {@link TableConfig} interface so that
     * the parlor services can determine how to configure the table that
     * will be created.
     *
     * @param creator the body object that will own the new table.
     * @param placeOid the place object id of the place (lobby) in which
     * this table should be created. The place object specified must
     * implement the {@link TableLobbyObject} interface.
     * @param config the configuration of the game to be created.
     *
     * @return the id of the newly created table.
     *
     * @exception ServiceFailedException thrown if the table creation was
     * not able processed for some reason. The explanation will be
     * provided in the message data of the exception.
     */
    public int createTable (BodyObject creator, int placeOid,
                            GameConfig config)
        throws ServiceFailedException
    {
        return -1;
    }

    /**
     * Requests that the specified user be added to the specified table at
     * the specified position. If the user successfully joins the table,
     * the function will return normally. If they are not able to join for
     * some reason (the slot is already full, etc.), a {@link
     * ServiceFailedException} will be thrown with a message code that
     * describes the reason for failure. If the user does successfully
     * join, they will be added to the table entry in the tables set in
     * the place object that is hosting the table.
     *
     * @param joiner the body object of the user that wishes to join the
     * table.
     * @param tableId the id of the table to be joined.
     * @param position the position at which to join the table.
     *
     * @return the id of the newly created table.
     *
     * @exception ServiceFailedException thrown if the table creation was
     * not able processed for some reason. The explanation will be
     * provided in the message data of the exception.
     */
    public void joinTable (BodyObject joiner, int tableId, int position)
        throws ServiceFailedException
    {
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
