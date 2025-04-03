//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

import com.threerings.util.ActionScript;

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

    /** The client side 'localtype' of this chat, set to the type registered with an auxiliary
     * source in the ChatDirector. */
    public transient String localtype;

    /** The client time that this message was created. */
    @ActionScript(type="int")
    public transient long timestamp;

    /**
     * Construct a ChatMessage.
     */
    public ChatMessage (String message, String bundle)
    {
        this.message = message;
        this.bundle = bundle;
    }

    /**
     * Once this message reaches the client, the information contained within is changed around a
     * bit.
     */
    public void setClientInfo (String msg, String ltype)
    {
        message = msg;
        localtype = ltype;
        bundle = null;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Get the appropriate message format for this message.
     */
    public String getFormat ()
    {
        return null;
    }

    @Override
    public String toString ()
    {
        return StringUtil.shortClassName(this) + StringUtil.fieldsToString(this);
    }
}
