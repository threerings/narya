//
// $Id: InvitationHandler.java,v 1.4 2004/02/25 14:44:54 mdb Exp $

package com.threerings.parlor.client;

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
     * @param invite the received invitation.
     */
    public void invitationReceived (Invitation invite);

    /**
     * Called when an invitation is cancelled by the inviting player.
     *
     * @param invite the cancelled invitation.
     */
    public void invitationCancelled (Invitation invite);
}
