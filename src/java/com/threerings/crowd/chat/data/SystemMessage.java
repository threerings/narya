//
// $Id: SystemMessage.java,v 1.1 2002/07/26 20:35:01 ray Exp $

package com.threerings.crowd.chat;

/**
 * A ChatMessage that represents a message that came from the server
 * and did not result from direct user action.
 */
public class SystemMessage extends ChatMessage
{
    /**
     * Construct a SystemMessage.
     */
    public SystemMessage (String message, String localtype)
    {
        super(message, localtype);
    }
}
