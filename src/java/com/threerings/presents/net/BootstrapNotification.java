//
// $Id: BootstrapNotification.java,v 1.4 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A bootstrap notification is delivered to the client once the server has
 * fully initialized itself in preparation for dealing with this client.
 * The authentication process completes very early and further information
 * need be communicated to the client so that it can fully interact with
 * the server. This information is communicated via the bootstrap
 * notification.
 */
public class BootstrapNotification extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public BootstrapNotification ()
    {
        super();
    }

    /**
     * Constructs an bootstrap notification with the supplied data.
     */
    public BootstrapNotification (BootstrapData data)
    {
        _data = data;
    }

    public BootstrapData getData ()
    {
        return _data;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeObject(_data);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _data = (BootstrapData)in.readObject();
    }

    public String toString ()
    {
        return "[type=BOOT, msgid=" + messageId + ", data=" + _data + "]";
    }

    /** The data associated with this notification. */
    protected BootstrapData _data;
}
