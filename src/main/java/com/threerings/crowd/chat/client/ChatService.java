//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
