//
// $Id: ChatMessage.java,v 1.2 2003/01/21 01:08:10 mdb Exp $

package com.threerings.crowd.chat;

import com.samskivert.util.StringUtil;

/**
 * The abstract base class of all the client-side ChatMessage objects.
 */
public abstract class ChatMessage
{
    /** The actual text of the message. */
    public String message;

    /** The localtype of this chat, set to the type registered with an
     * auxiliary source in the ChatDirector. */
    public String localtype;

    /** The time that this message was created on the client. */
    public long timestamp;

    /**
     * Construct a ChatMessage.
     */
    public ChatMessage (String message, String localtype)
    {
        this.message = message;
        this.localtype = localtype;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.shortClassName(this) + "[msg=" + message +
            ", type=" + localtype + ", stamp=" + timestamp + "]";
    }
}
