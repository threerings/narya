//
// $Id: ChatCodes.java,v 1.15 2003/03/30 02:53:05 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the chat invocation services.
 */
public interface ChatCodes extends InvocationCodes
{
    /** The chat localtype code for chat messages delivered on the place
     * object currently occupied by the client. This is the only type of
     * chat message that will be delivered unless the chat director is
     * explicitly provided with other chat message sources via {@link
     * ChatDirector#addAuxiliarySource}. */
    public static final String PLACE_CHAT_TYPE = "placeChat";

    /** The message identifier for a speak notification message. */
    public static final String SPEAK_NOTIFICATION = "spknot";

    /** The message identifier for a system notification message. */
    public static final String SYSTEM_NOTIFICATION = "sysnot";

    /** The chat localtype for tells. */
    public static final String TELL_CHAT_TYPE = "tellChat";

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

    /** String translations for the various chat modes. */
    public static final String[] XLATE_MODES = {
        "default", "think", "emote", "shout", "broadcast" };

    /** An error code delivered when the user targeted for a tell
     * notification is not online. */
    public static final String USER_NOT_ONLINE = "m.user_not_online";

    /** An error code delivered when the user targeted for a tell
     * notification is disconnected. */
    public static final String USER_DISCONNECTED = "m.user_disconnected";
}
