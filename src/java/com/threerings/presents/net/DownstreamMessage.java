//
// $Id: DownstreamMessage.java,v 1.9 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

/**
 * This class encapsulates a message in the distributed object protocol
 * that flows from the server to the client. Downstream messages include
 * object subscription, event forwarding and session management.
 */
public abstract class DownstreamMessage extends SimpleStreamableObject
{
    /**
     * The message id of the upstream message with which this downstream
     * message is associated (or -1 if it is not associated with any
     * upstream message).
     */
    public short messageId = -1;

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

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }
}
