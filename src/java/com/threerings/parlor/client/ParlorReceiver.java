//
// $Id: ParlorReceiver.java,v 1.3 2004/03/06 11:29:19 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationReceiver;

import com.threerings.parlor.game.GameConfig;

/**
 * Defines, for the parlor services, a set of notifications delivered
 * asynchronously by the server to the client. These are handled by the
 * {@link ParlorDirector}.
 */
public interface ParlorReceiver extends InvocationReceiver
{
    /**
     * Dispatched to the client when a game in which they are a
     * participant is ready for play. The client will then enter the game
     * room which will trigger the loading of the appropriate game UI code
     * and generally get things started.
     *
     * @param gameOid the object id of the game object.
     */
    public void gameIsReady (int gameOid);

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
    public void receivedInvite (int remoteId, Name inviter, GameConfig config);

    /**
     * Called by the invocation services when another user has responded
     * to our invitation by either accepting, refusing or countering it.
     *
     * @param remoteId the indentifier for the invitation on question.
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
    public void receivedInviteResponse (int remoteId, int code, Object arg);

    /**
     * Called by the invocation services when an outstanding invitation
     * has been cancelled by the inviting user.
     *
     * @param remoteId the indentifier of the cancelled invitation.
     */
    public void receivedInviteCancellation (int remoteId);
}
