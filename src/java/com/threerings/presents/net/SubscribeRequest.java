//
// $Id: SubscribeRequest.java,v 1.6 2002/07/23 05:52:49 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class SubscribeRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public SubscribeRequest ()
    {
        super();
    }

    /**
     * Constructs a subscribe request for the distributed object with the
     * specified object id.
     */
    public SubscribeRequest (int oid)
    {
        _oid = oid;
    }

    /**
     * Returns the oid of the object to which we desire subscription.
     */
    public int getOid ()
    {
        return _oid;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeInt(_oid);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _oid = in.readInt();
    }

    public String toString ()
    {
        return "[type=SUB, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    /**
     * The object id of the distributed object to which we are
     * subscribing.
     */
    protected int _oid;
}
