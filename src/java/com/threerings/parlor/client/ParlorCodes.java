//
// $Id: ParlorCodes.java,v 1.4 2001/10/04 22:47:49 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.cocktail.cher.client.InvocationCodes;

/**
 * Contains codes used by the parlor invocation services.
 */
public interface ParlorCodes extends InvocationCodes
{
    /** The module name for the parlor services. */
    public static final String MODULE_NAME = "parlor";

    /** The message identifier for a game ready notification. This is
     * mapped by the invocation services to a call to {@link
     * ParlorDirector#handleGameReadyNotification}. */
    public static final String GAME_READY_NOTIFICATION = "GameReady";

    /** The message identifier for an invitation creation request or
     * notification. The notification is mapped by the invocation services
     * to a call to {@link ParlorDirector#handleInviteNotification}. */
    public static final String INVITE_ID = "Invite";

    /** The response identifier for an accepted invite request. This is
     * mapped by the invocation services to a call to {@link
     * ParlorDirector#handleInviteReceived}. */
    public static final String INVITE_RECEIVED_RESPONSE = "InviteReceived";

    /** The response identifier for a rejceted invite request. This is
     * mapped by the invocation services to a call to {@link
     * ParlorDirector#handleInviteFailed}. */
    public static final String INVITE_FAILED_RESPONSE = "InviteFailed";

    /** The message identifier for an invitation cancellation request or
     * notification. The notification is mapped by the invocation services
     * to a call to {@link
     * ParlorDirector#handleCancelInviteNotification}. */
    public static final String CANCEL_INVITE_ID = "CancelInvite";

    /** The message identifier for an invitation response request or
     * notification. The notification is mapped by the invocation services
     * to a call to {@link
     * ParlorDirector#handleRespondInviteNotification}. */
    public static final String RESPOND_INVITE_ID = "RespondInvite";

    /** The response code for an accepted invitation. */
    public static final int INVITATION_ACCEPTED = 0;

    /** The response code for a refused invitation. */
    public static final int INVITATION_REFUSED = 1;

    /** The response code for a countered invitation. */
    public static final int INVITATION_COUNTERED = 2;

    /** An error code explaining that an invitation was rejected because
     * the invited user was not online at the time the invitation was
     * received. */
    public static final String INVITEE_NOT_ONLINE = "m.invitee_not_online";
}
