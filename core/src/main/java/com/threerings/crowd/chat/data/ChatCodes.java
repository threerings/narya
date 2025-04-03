//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.data.Permission;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.SpeakService;

/**
 * Contains codes used by the chat invocation services.
 */
public interface ChatCodes extends InvocationCodes
{
    /** A return value used by the ChatDirector and possibly other entities to indicate successful
     * processing of chat. */
    public static final String SUCCESS = "success";

    /** The message identifier for a chat notification message. */
    public static final String CHAT_NOTIFICATION = "crowd.chat";

    /** The message identifier for a chat channel notification message. */
    public static final String CHAT_CHANNEL_NOTIFICATION = "crowd.chat.channel";

    /** The access control identifier for normal chat privileges. */
    public static final Permission CHAT_ACCESS = new Permission();

    /** The access control identifier for broadcast chat privileges. */
    public static final Permission BROADCAST_ACCESS = new Permission();

    /** The configuration key for idle time. */
    public static final String IDLE_TIME_KEY = "narya.chat.idle_time";

    /** The default time after which a player is assumed idle. */
    public static final long DEFAULT_IDLE_TIME = 3 * 60 * 1000L;

    /** The chat localtype code for chat messages delivered on the place object currently occupied
     * by the client. This is the only type of chat message that will be delivered unless the chat
     * director is explicitly provided with other chat message sources via {@link
     * ChatDirector#addAuxiliarySource}. */
    public static final String PLACE_CHAT_TYPE = "placeChat";

    /** The chat localtype for messages received on the user object. */
    public static final String USER_CHAT_TYPE = "userChat";

    /** The default mode used by {@link SpeakService#speak} requests. */
    public static final byte DEFAULT_MODE = 0;

    /** A {@link SpeakService#speak} mode to indicate that the user is thinking what they're
     * saying, or is it that they're saying what they're thinking? */
    public static final byte THINK_MODE = 1;

    /** A {@link SpeakService#speak} mode to indicate that a speak is actually an emote. */
    public static final byte EMOTE_MODE = 2;

    /** A {@link SpeakService#speak} mode to indicate that a speak is actually a shout. */
    public static final byte SHOUT_MODE = 3;

    /** A {@link SpeakService#speak} mode to indicate that a speak is actually a server-wide
     * broadcast. */
    public static final byte BROADCAST_MODE = 4;

    /** The last chat mode defined in the interface. */
    public static final byte LAST_MODE = BROADCAST_MODE;

    /** String translations for the various chat modes. */
    public static final String[] XLATE_MODES = {
        "default", "think", "emote", "shout", "broadcast"
    };

    /** An error code delivered when the user targeted for a tell notification is not online. */
    public static final String USER_NOT_ONLINE = "m.user_not_online";

    /** An error code delivered when the user targeted for a tell notification is disconnected. */
    public static final String USER_DISCONNECTED = "m.user_disconnected";
}
