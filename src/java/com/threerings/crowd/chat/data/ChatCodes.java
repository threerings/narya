//
// $Id: ChatCodes.java,v 1.4 2001/12/16 21:46:46 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.client.InvocationCodes;

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
     * ChatDirector#addAuxilliarySource}. */
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
     * ChatDirector#handleTellSucceded}. */
    public static final String TELL_SUCCEEDED_RESPONSE = "TellSucceeded";

    /** The response identifier for a failed tell request. This is mapped
     * by the invocation services to a call to {@link
     * ChatDirector#handleTellFailed}. */
    public static final String TELL_FAILED_RESPONSE = "TellFailed";

    /** The message identifier for a tell notification. */
    public static final String TELL_NOTIFICATION = "Tell";

    /** An error code delivered when the user targeted for a tell
     * notification is not online. */
    public static final String USER_NOT_ONLINE = "m.user_not_online";
}
