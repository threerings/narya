//
// $Id: InvitationHandler.java,v 1.2 2001/10/11 21:08:21 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.parlor.game.GameConfig;

/**
 * A client entity that wishes to handle invitations received by other
 * clients should implement this interface and register itself with the
 * parlor director. It will subsequently be notified of any incoming
 * invitations. It is also responsible for handling cancelled invitations.
 */
public interface InvitationHandler
{
    /**
     * Called when an invitation is received from another player.
     *
     * @param inviteId this invitation's unique id.
     * @param inviter the username of the user that sent the invitation.
     * @param config the configuration of the game to which we are being
     * invited.
     */
    public void invitationReceived (int inviteId, String inviter,
                                    GameConfig config);

    /**
     * Called when an invitation is cancelled by the inviting player.
     *
     * @param inviteId this invitation's unique id.
     */
    public void invitationCancelled (int inviteId);
}
