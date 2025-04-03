//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * This class encapsulates a message in the distributed object protocol that flows from the client
 * to the server.  Upstream messages include object subscription, event forwarding and session
 * management.
 */
public abstract class UpstreamMessage extends Message
{
    /**
     * This is a unique (within the context of a reasonable period of time) identifier assigned to
     * each upstream message. The message ids are used to correlate a downstream response message
     * to the appropriate upstream request message.
     */
    public short messageId;

    /**
     * Each upstream message derived class must provide a zero argument constructor so that it can
     * be unserialized when read from the network.
     */
    public UpstreamMessage ()
    {
        // automatically generate a valid message id; on the client, this
        // will be used, on the server it will be overwritten by the
        // unserialized value
        this.messageId = nextMessageId();
    }

    @Override
    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }

    /**
     * Returns the next message id suitable for use by an upstream message.
     */
    protected static synchronized short nextMessageId ()
    {
        _nextMessageId = (short)((_nextMessageId + 1) % Short.MAX_VALUE);
        return _nextMessageId;
    }

    /**
     * This is used to generate monotonically increasing message ids on the client as new messages
     * are generated.
     */
    protected static short _nextMessageId;
}
