//
// $Id: UpstreamMessage.java,v 1.7 2001/07/19 19:30:14 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.io.TypedObject;

/**
 * The <code>UpstreamMessage</code> class encapsulates a message in the
 * Distributed Object Protocol that flows from the client to the server.
 * Upstream messages include object subscription, event forwarding and
 * session management.
 */
public abstract class UpstreamMessage implements TypedObject
{
    /**
     * All upstream message derived classes should base their typed object
     * code on this base value.
     */
    public static final short TYPE_BASE = 100;

    /**
     * This is a unique (within the context of a reasonable period of
     * time) identifier assigned to each upstream message. The message ids
     * are used to correlate a downstream response message to the
     * appropriate upstream request message.
     */
    public short messageId;

    /**
     * Each upstream message derived class must provide a zero argument
     * constructor so that the <code>TypedObjectFactory</code> can create
     * a new instance of said class prior to unserializing it.
     */
    public UpstreamMessage ()
    {
        // automatically generate a valid message id; on the client, this
        // will be used, on the server it will be overwritten by the
        // unserialized value
        this.messageId = nextMessageId();
    }

    /**
     * Derived classes should override this function to write their fields
     * out to the supplied data output stream. They <em>must</em> be sure
     * to first call <code>super.writeTo()</code>.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeShort(messageId);
    }

    /**
     * Derived classes should override this function to read their fields
     * from the supplied data input stream. They <em>must</em> be sure to
     * first call <code>super.readFrom()</code>.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        messageId = in.readShort();
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
