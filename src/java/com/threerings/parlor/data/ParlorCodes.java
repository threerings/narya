//
// $Id: ParlorCodes.java,v 1.3 2002/04/17 18:26:29 mdb Exp $

package com.threerings.parlor.data;

import com.threerings.presents.data.InvocationCodes;

import com.threerings.parlor.client.ParlorDirector;
import com.threerings.parlor.client.TableDirector;

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

    /** The message identifier for a create table request. */
    public static final String CREATE_TABLE_REQUEST = "CreateTable";

    /** The response identifier for a table created response. This is
     * mapped by the invocation services to a call to {@link
     * TableDirector#handleTableCreated}. */
    public static final String TABLE_CREATED_RESPONSE = "TableCreated";

    /** The message identifier for a join table request. */
    public static final String JOIN_TABLE_REQUEST = "JoinTable";

    /** The message identifier for a leave table request. */
    public static final String LEAVE_TABLE_REQUEST = "LeaveTable";

    /** An error code returned when a user requests to join a table that
     * doesn't exist. */
    public static final String NO_SUCH_TABLE = "m.no_such_table";

    /** An error code returned when a user requests to join a table at a
     * position that is not valid. */
    public static final String INVALID_TABLE_POSITION =
        "m.invalid_table_position";

    /** An error code returned when a user requests to join a table in a
     * position that is already occupied. */
    public static final String TABLE_POSITION_OCCUPIED =
        "m.table_position_occupied";

    /** An error code returned when a user requests to leave a table that
     * they were not sitting at in the first place. */
    public static final String NOT_AT_TABLE = "m.not_at_table";
}
