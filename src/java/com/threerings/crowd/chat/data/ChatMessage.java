//
// $Id: ChatMessage.java,v 1.4 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

/**
 * The abstract base class of all the client-side ChatMessage objects.
 */
public abstract class ChatMessage
    implements Streamable
{
    /** The actual text of the message. */
    public String message;

    /** The bundle to use when translating this message. */
    public String bundle;

    /** The client side 'localtype' of this chat, set to the type
     * registered with an auxiliary source in the ChatDirector. */
    public transient String localtype;

    /** The client time that this message was created. */
    public transient long timestamp;

    /**
     * For all your unserialization needs.
     */
    public ChatMessage ()
    {
    }

    /**
     * Construct a ChatMessage.
     */
    public ChatMessage (String message, String bundle)
    {
        this.message = message;
        this.bundle = bundle;
    }

    /**
     * Once this message reaches the client, the information contained within
     * is changed around a bit.
     */
    public void setClientInfo (String msg, String localtype)
    {
        message = msg;
        this.localtype = localtype;
        bundle = null;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.shortClassName(this) +
            StringUtil.fieldsToString(this);
    }
}
