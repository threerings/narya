//
// $Id: ChatService.java,v 1.8 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

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
         */
        public void tellSucceeded ();
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
    public void tell (Client client, String target, String message,
                      TellListener listener);
}
