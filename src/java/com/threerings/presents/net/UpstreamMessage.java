//
// $Id: UpstreamMessage.java,v 1.13 2004/08/23 21:05:04 mdb Exp $

package com.threerings.presents.net;

import com.threerings.io.TrackedStreamableObject;

/**
 * This class encapsulates a message in the distributed object protocol
 * that flows from the client to the server.  Upstream messages include
 * object subscription, event forwarding and session management.
 */
public abstract class UpstreamMessage extends TrackedStreamableObject
{
    /**
     * This is a unique (within the context of a reasonable period of
     * time) identifier assigned to each upstream message. The message ids
     * are used to correlate a downstream response message to the
     * appropriate upstream request message.
     */
    public short messageId;

    /** A timestamp indicating when this upstream message was received. */
    public transient long received;

    /**
     * Each upstream message derived class must provide a zero argument
     * constructor so that it can be unserialized when read from the
     * network.
     */
    public UpstreamMessage ()
    {
        // automatically generate a valid message id; on the client, this
        // will be used, on the server it will be overwritten by the
        // unserialized value
        this.messageId = nextMessageId();
    }

    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }

    /**
     * Returns the next message id suitable for use by an upstream
     * message.
     */
    protected static synchronized short nextMessageId ()
    {
        _nextMessageId = (short)((_nextMessageId + 1) % Short.MAX_VALUE);
        return _nextMessageId;
    }

    /**
     * This is used to generate monotonically increasing message ids on
     * the client as new messages are generated.
     */
    protected static short _nextMessageId;
}
