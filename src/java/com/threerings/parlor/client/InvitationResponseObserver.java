//
// $Id: InvitationResponseObserver.java,v 1.5 2001/10/11 21:08:21 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.parlor.game.GameConfig;

/**
 * A client entity that wishes to generate invitations for games must
 * implement this interface. An invitation can be accepted, refused or
 * countered. A countered invitation is one where the game configuration
 * is adjusted by the invited player and proposed back to the inviting
 * player.
 */
public interface InvitationResponseObserver
{
    /**
     * Called if the invitation was accepted.
     *
     * @param inviteId the unique id of the invitation for which we
     * received a response.
     */
    public void invitationAccepted (int inviteId);

    /**
     * Called if the invitation was refused.
     *
     * @param inviteId the unique id of the invitation for which we
     * received a response.
     * @param message a message provided by the invited user explaining
     * the reason for their refusal, or the empty string if no message was
     * provided.
     */
    public void invitationRefused (int inviteId, String message);

    /**
     * Called if the invitation was countered with an alternate game
     * configuration.
     *
     * @param inviteId the unique id of the invitation for which we
     * received a response.
     * @param config the game configuration proposed by the invited
     * player.
     */
    public void invitationCountered (int inviteId, GameConfig config);
}
