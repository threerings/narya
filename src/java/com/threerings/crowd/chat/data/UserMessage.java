//
// $Id: UserMessage.java,v 1.1 2002/07/26 20:35:01 ray Exp $

package com.threerings.crowd.chat;

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
     * Construct a user message.
     */
    public UserMessage (String message, String localtype,
                        String speaker, byte mode)
    {
        super(message, localtype);
        this.speaker = speaker;
        this.mode = mode;
    }
}
