//
// $Id: ParlorService.java,v 1.11 2001/10/26 01:40:22 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.parlor.Log;
import com.threerings.parlor.game.GameConfig;

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
public class ParlorService implements ParlorCodes
{
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
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int invite (Client client, String invitee,
                              GameConfig config, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { invitee, config };
        return invdir.invoke(MODULE_NAME, INVITE_ID, args, rsptgt);
    }

    /**
     * You probably don't want to call this directly, but want to call one
     * of {@link ParlorDirector#accept}, {@link ParlorDirector#refuse}, or
     * {@link ParlorDirector#counter}. Requests that an invitation
     * response be delivered with the specified parameters.
     *
     * @param client a connected, operational client instance.
     * @param inviteId the unique id previously assigned by the server to
     * this invitation.
     * @param code the response code to use in responding to the
     * invitation.
     * @param arg the argument associated with the response (a string
     * message from the player explaining why the response was refused in
     * the case of an invitation refusal or an updated game configuration
     * object in the case of a counter-invitation, or null in the case of
     * an accepted invitation).
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int respond (Client client, int inviteId, int code,
                               Object arg, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            new Integer(inviteId), new Integer(code), null };
        // we can't have a null argument so we use the empty string
        args[2] = (arg == null) ? "" : arg;
        return invdir.invoke(MODULE_NAME, RESPOND_INVITE_ID, args, rsptgt);
    }

    /**
     * You probably don't want to call this directly, but want to call
     * {@link ParlorDirector#cancel}. Requests that an outstanding
     * invitation be cancelled.
     *
     * @param client a connected, operational client instance.
     * @param inviteId the unique id previously assigned by the server to
     * this invitation.
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int cancel (Client client, int inviteId, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(inviteId) };
        return invdir.invoke(MODULE_NAME, CANCEL_INVITE_ID, args, rsptgt);
    }

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#createTable}. Requests that a new table be
     * created.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that will contain the newly
     * created table.
     * @param config the game config for the game to be matchmade by the
     * table.
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int createTable (
        Client client, int lobbyOid, GameConfig config, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(lobbyOid), config };
        return invdir.invoke(MODULE_NAME, CREATE_TABLE_REQUEST, args, rsptgt);
    }

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#joinTable}. Requests that the current user
     * be added to the specified table at the specified position.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that contains the table.
     * @param tableId the unique id of the table to which this user wishes
     * to be added.
     * @param position the position at the table to which this user desires
     * to be added.
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int joinTable (Client client, int lobbyOid, int tableId,
                                 int position, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(lobbyOid),
                                       new Integer(tableId),
                                       new Integer(position) };
        return invdir.invoke(MODULE_NAME, JOIN_TABLE_REQUEST, args, rsptgt);
    }

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#leaveTable}. Requests that the current user
     * be removed from the specified table.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that contains the table.
     * @param tableId the unique id of the table from which this user
     * wishes to be removed.
     * @param rsptgt the object reference that will receive and process
     * the response.
     *
     * @return the invocation request id of the generated request.
     */
    public static int leaveTable (
        Client client, int lobbyOid, int tableId, Object rsptgt)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(lobbyOid),
                                       new Integer(tableId) };
        return invdir.invoke(MODULE_NAME, LEAVE_TABLE_REQUEST, args, rsptgt);
    }
}
