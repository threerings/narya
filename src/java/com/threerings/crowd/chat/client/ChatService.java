//
// $Id: ChatService.java,v 1.13 2004/03/06 11:29:18 mdb Exp $

package com.threerings.crowd.chat.client;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * The chat services provide a mechanism by which the client can broadcast
 * chat messages to all clients that are subscribed to a particular place
 * object or directly to a particular client. These services should not be
 * used directly, but instead should be accessed via the {@link
 * ChatDirector}.
 */
public interface ChatService extends InvocationService
{
    /**
     * Used to communicate the response to a {@link #tell} request.
     */
    public static interface TellListener extends InvocationListener
    {
        /**
         * Communicates the response to a {@link #tell} request.
         *
         * @param idletime the number of ms the tellee has been idle or 0L
         * if they are not idle.
         * @param awayMessage the away message configured by the told
         * player or null if they have no away message.
         */
        public void tellSucceeded (long idleTime, String awayMessage);
    }

    /**
     * Requests that a tell message be delivered to the user with username
     * equal to <code>target</code>.
     *
     * @param client a connected, operational client instance.
     * @param target the username of the user to which the tell message
     * should be delivered.
     * @param message the contents of the message.
     * @param listener the reference that will receive the tell response.
     */
    public void tell (Client client, Name target, String message,
                      TellListener listener);

    /**
     * Requests that a message be broadcast to all users in the system.
     *
     * @param client a connected, operational client instance.
     * @param message the contents of the message.
     * @param listener the reference that will receive a failure response.
     */
    public void broadcast (Client client, String message,
                           InvocationListener listener);

    /**
     * Sets this client's away message. If the message is null or the
     * empty string, the away message will be cleared.
     */
    public void away (Client client, String message);
}
