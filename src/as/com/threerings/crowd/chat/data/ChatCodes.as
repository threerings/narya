//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.chat.data {

import com.threerings.presents.data.InvocationCodes;

import com.threerings.crowd.data.BodyObject;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.SpeakService;

/**
 * Contains codes used by the chat invocation services.
 */
public class ChatCodes extends InvocationCodes
{
    /** A return value used by the ChatDirector and possibly other entities
     * to indicate successful processing of chat. */
    public static const SUCCESS :String = "success";

    /** The message identifier for a chat notification message. */
    public static const CHAT_NOTIFICATION :String = "chat";

    /** The access control identifier for normal chat privileges. See
     * {@link BodyObject#checkAccess}. */
    public static const CHAT_ACCESS :String = "crowd.chat.chat";

    /** The access control identifier for broadcast chat privileges. See
     * {@link BodyObject#checkAccess}. */
    public static const BROADCAST_ACCESS :String = "crowd.chat.broadcast";

    /** The configuration key for idle time. */
    public static const IDLE_TIME_KEY :String = "narya.chat.idle_time";

    /** The default time after which a player is assumed idle. */
    public static const DEFAULT_IDLE_TIME :Number = 3 * 60 * 1000;

    /** The chat localtype code for chat messages delivered on the place
     * object currently occupied by the client. This is the only type of
     * chat message that will be delivered unless the chat director is
     * explicitly provided with other chat message sources via {@link
     * ChatDirector#addAuxiliarySource}. */
    public static const PLACE_CHAT_TYPE :String = "placeChat";

    /** The chat localtype for messages received on the user object. */
    public static const USER_CHAT_TYPE :String = "userChat";

    /** The default mode used by {@link SpeakService#speak} requests. */
    public static const DEFAULT_MODE :int = 0;

    /** A {@link SpeakService#speak} mode to indicate that the user is
     * thinking what they're saying, or is it that they're saying what
     * they're thinking? */
    public static const THINK_MODE :int = 1;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually an emote. */
    public static const EMOTE_MODE :int = 2;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually a shout. */
    public static const SHOUT_MODE :int = 3;

    /** A {@link SpeakService#speak} mode to indicate that a speak is
     * actually a server-wide broadcast. */
    public static const BROADCAST_MODE :int = 4;

    /** An error code delivered when the user targeted for a tell
     * notification is not online. */
    public static const USER_NOT_ONLINE :String = "m.user_not_online";

    /** An error code delivered when the user targeted for a tell
     * notification is disconnected. */
    public static const USER_DISCONNECTED :String = "m.user_disconnected";
}
}
