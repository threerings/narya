//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

/**
 * The chat services provide a mechanism by which the client can broadcast chat messages to all
 * clients that are subscribed to a particular place object or directly to a particular client.
 * These services should not be used directly, but instead should be accessed via the
 * {@link ChatDirector}.
 */
public interface ChatService extends InvocationService<ClientObject>
{
    /**
     * Used to communicate the response to a {@link ChatService#tell} request.
     */
    public static interface TellListener extends InvocationListener
    {
        /**
         * Communicates the response to a {@link ChatService#tell} request.
         *
         * @param idleTime the number of ms the tellee has been idle or 0L if they are not idle.
         * @param awayMessage the away message configured by the told player or null if they have
         * no away message.
         */
        void tellSucceeded (long idleTime, String awayMessage);
    }

    /**
     * Requests that a tell message be delivered to the user with username equal to
     * <code>target</code>.
     *
     * @param target the username of the user to which the tell message should be delivered.
     * @param message the contents of the message.
     * @param listener the reference that will receive the tell response.
     */
    void tell (Name target, String message, TellListener listener);

    /**
     * Requests that a message be broadcast to all users in the system.
     *
     * @param message the contents of the message.
     * @param listener the reference that will receive a failure response.
     */
    void broadcast (String message, InvocationListener listener);

    /**
     * Sets this client's away message. If the message is null or the empty string, the away
     * message will be cleared.
     */
    void away (String message);
}
