//
// $Id: ParlorService.java,v 1.1 2001/10/01 02:56:35 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationManager;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.GameConfig;

/**
 * This class provides an interface to the various parlor services that
 * are directly invokable by the client (by means of the invocation
 * services). Presently these services are limited to the various
 * matchmaking mechanisms. It is unlikely that client code will want to
 * make direct use of this class, instead they would make use of the
 * programmatic interface provided by the {@link ParlorDirector}.
 *
 * @see ParlorDirector
 */
public class ParlorService
{
    /** The module name for the parlor services. */
    public static final String MODULE = "parlor";

    /** The message identifier for an invitation creation request. */
    public static final String INVITE_REQUEST = "Invite";

    /** The message identifier for an invitation notification. */
    public static final String INVITE_NOTIFICATION = "Invite";

    /** The message identifier for an invitation response request. */
    public static final String RESPONSE_REQUEST = "Response";

    /** The message identifier for an invitation response notification. */
    public static final String RESPONSE_NOTIFICATION = "Response";

    /** The response code for an accepted invitation. */
    public static final int INVITATION_ACCEPTED = 0;

    /** The response code for a rejected invitation. */
    public static final int INVITATION_REJECTED = 1;

    /** The message identifier for an counter invitation request. */
    public static final String COUNTER_REQUEST = "Counter";

    /** The message identifier for an countered invitation notification. */
    public static final String COUNTER_NOTIFICATION = "Counter";

    /** The message identifier for an rescind invitation request. */
    public static final String RESCIND_REQUEST = "Rescind";

    /** The message identifier for an rescinded invitation notification. */
    public static final String RESCIND_NOTIFICATION = "Rescind";

    /**
     * You probably don't want to call this directly, but want to generate
     * your invitation request via {@link ParlorDirector#invite}. Requests
     * that an invitation be delivered to the named user, requesting that
     * they join the inviting user in a game, the details of which are
     * specified in the supplied game config object.
     *
     * @param client a connected, operational client instance.
     * @param invitee the username of the user to be invited.
     * @param config a game config object detailing the type and
     * configuration of the game to be created.
     * @param rsptarget the parlor director reference that will receive and
     * process the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int invite (Client client, String invitee,
                              GameConfig config, Object rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[] { invitee, config };
        Log.info("Sending invite request [to=" + invitee +
                 ", cfg=" + config + "].");
        return invmgr.invoke(MODULE, INVITE_REQUEST, args, rsptarget);
    }
}
