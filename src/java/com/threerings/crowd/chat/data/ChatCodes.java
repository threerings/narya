//
// $Id: ChatCodes.java,v 1.20 2004/08/27 02:12:31 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.crowd.chat.data;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the chat invocation services.
 */
public interface ChatCodes extends InvocationCodes
{
    /** The message identifier for a chat notification message. */
    public static final String CHAT_NOTIFICATION = "chat";

    /** The configuration key for idle time. */
    public static final String IDLE_TIME_KEY = "narya.chat.idle_time";

    /** The default time after which a player is assumed idle. */
    public static final long DEFAULT_IDLE_TIME = 3 * 60 * 1000L;

    /** The chat localtype code for chat messages delivered on the place
     * object currently occupied by the client. This is the only type of
     * chat message that will be delivered unless the chat director is
     * explicitly provided with other chat message sources via {@link
     * ChatDirector#addAuxiliarySource}. */
    public static final String PLACE_CHAT_TYPE = "placeChat";

    /** The chat localtype for messages received on the user object. */
    public static final String USER_CHAT_TYPE = "userChat";

    /** The default mode used by {@link SpeakService#speak} requests. */
    public static final byte DEFAULT_MODE = 0;

    /** A {@link SpeakService#speak} mode to indicate that the user is
     * thinking what they're saying, or is it that they're saying what
     * they're thinking? */
    public static final byte THINK_MODE = 1;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually an emote. */
    public static final byte EMOTE_MODE = 2;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually a shout. */
    public static final byte SHOUT_MODE = 3;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually a server-wide broadcast. */
    public static final byte BROADCAST_MODE = 4;

    /** An error code delivered when the user targeted for a tell
     * notification is not online. */
    public static final String USER_NOT_ONLINE = "m.user_not_online";

    /** An error code delivered when the user targeted for a tell
     * notification is disconnected. */
    public static final String USER_DISCONNECTED = "m.user_disconnected";
}
