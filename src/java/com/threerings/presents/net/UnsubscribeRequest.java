//
// $Id: UnsubscribeRequest.java,v 1.7 2002/07/23 05:52:49 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class UnsubscribeRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UnsubscribeRequest ()
    {
        super();
    }

    /**
     * Constructs a unsubscribe request for the distributed object
     * with the specified object id.
     */
    public UnsubscribeRequest (int oid)
    {
        _oid = oid;
    }

    /**
     * Returns the oid of the object from which we are unsubscribing.
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
        return "[type=UNSUB, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    /**
     * The object id of the distributed object from which we are
     * unsubscribing.
     */
    protected int _oid;
}
