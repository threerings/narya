//
// $Id: ParlorCodes.java,v 1.1 2001/10/01 22:17:34 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.cocktail.cher.client.InvocationCodes;

/**
 * Contains codes used by the parlor invocation services.
 */
public interface ParlorCodes extends InvocationCodes
{
    /** The module name for the parlor services. */
    public static final String MODULE_NAME = "parlor";

    /** The message identifier for an invitation creation request or
     * notification. */
    public static final String INVITE_ID = "Invite";

    /** The message identifier for an invitation cancellation request or
     * notification. */
    public static final String CANCEL_INVITE_ID = "CancelInvite";

    /** The message identifier for an invitation response request or
     * notification. */
    public static final String RESPOND_INVITE_ID = "RespondInvite";

    /** The response code for an accepted invitation. */
    public static final int INVITATION_ACCEPTED = 0;

    /** The response code for a refused invitation. */
    public static final int INVITATION_REFUSED = 1;

    /** The response code for a countered invitation. */
    public static final int INVITATION_COUNTERED = 2;
}
