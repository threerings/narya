//
// $Id: UserMessage.java,v 1.2 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.data;

/**
 * A ChatMessage representing a message that came from another user.
 */
public class UserMessage extends ChatMessage
{
    /** The user that the message came from. */
    public String speaker;

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
    public UserMessage (String message, String bundle,
                        String speaker, byte mode)
    {
        super(message, bundle);
        this.speaker = speaker;
        this.mode = mode;
    }
}
