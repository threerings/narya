//
// $Id: UserMessage.java,v 1.3 2004/03/06 11:29:18 mdb Exp $

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

/**
 * A ChatMessage representing a message that came from another user.
 */
public class UserMessage extends ChatMessage
{
    /** The user that the message came from. */
    public Name speaker;

    /** The mode of the message. @see ChatCodes.DEFAULT_MODE */
    public byte mode;

    /**
     * For unserialization.
     */
    public UserMessage ()
    {
    }

    /**
     * Construct a user message.
     */
    public UserMessage (String message, String bundle, Name speaker, byte mode)
    {
        super(message, bundle);
        this.speaker = speaker;
        this.mode = mode;
    }
}
