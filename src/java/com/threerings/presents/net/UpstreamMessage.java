//
// $Id: UpstreamMessage.java,v 1.9 2002/07/23 05:52:49 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

/**
 * This class encapsulates a message in the distributed object protocol
 * that flows from the client to the server.  Upstream messages include
 * object subscription, event forwarding and session management.
 */
public abstract class UpstreamMessage extends SimpleStreamableObject
{
    /**
     * This is a unique (within the context of a reasonable period of
     * time) identifier assigned to each upstream message. The message ids
     * are used to correlate a downstream response message to the
     * appropriate upstream request message.
     */
    public short messageId;

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

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
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
