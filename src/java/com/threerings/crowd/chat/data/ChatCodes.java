//
// $Id: ChatCodes.java,v 1.9 2002/07/22 22:54:03 ray Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the chat invocation services.
 */
public interface ChatCodes extends InvocationCodes
{
    /** The module name for the chat services. */
    public static final String MODULE_NAME = "chat";

    /** The chat type code for chat messages delivered on the place object
     * currently occupied by the client. This is the only type of chat
     * message that will be delivered unless the chat director is
     * explicitly provided with other chat message sources via {@link
     * ChatDirector#addAuxiliarySource}. */
    public static final String PLACE_CHAT_TYPE = "placeChat";

    /** The message identifier for a speak request message. */
    public static final String SPEAK_REQUEST = "spkreq";

    /** The message identifier for a speak notification message. */
    public static final String SPEAK_NOTIFICATION = "spknot";

    /** The message identifier for a system notification message. */
    public static final String SYSTEM_NOTIFICATION = "sysnot";

    /** The message identifier for a tell request. */
    public static final String TELL_REQUEST = "Tell";

    /** The response identifier for a successful tell request. This is
     * mapped by the invocation services to a call to {@link
     * ChatDirector#handleTellSucceeded}. */
    public static final String TELL_SUCCEEDED_RESPONSE = "TellSucceeded";

    /** The response identifier for a failed tell request. This is mapped
     * by the invocation services to a call to {@link
     * ChatDirector#handleTellFailed}. */
    public static final String TELL_FAILED_RESPONSE = "TellFailed";

    /** The message identifier for a tell notification. */
    public static final String TELL_NOTIFICATION = "Tell";

    /** The default mode used by speak requests. */
    public static final byte DEFAULT_MODE = 0;

    /** The think mode to indicate that the user is thinking
     * what they're saying, or is it that they're saying what they're
     * thinking? */
    public static final byte THINK_MODE = 1;

    /** The mode to indicate that a speak is actually an emote. */
    public static final byte EMOTE_MODE = 2;

    /** An error code delivered when the user targeted for a tell
     * notification is not online. */
    public static final String USER_NOT_ONLINE = "m.user_not_online";
}
