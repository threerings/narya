//
// $Id: SystemMessage.java,v 1.2 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.data;

/**
 * A ChatMessage that represents a message that came from the server
 * and did not result from direct user action.
 */
public class SystemMessage extends ChatMessage
{
    public SystemMessage ()
    {
    }

    /**
     * Construct a SystemMessage.
     */
    public SystemMessage (String message, String bundle)
    {
        super(message, bundle);
    }
}
