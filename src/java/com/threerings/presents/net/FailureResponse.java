//
// $Id: FailureResponse.java,v 1.8 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class FailureResponse extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public FailureResponse ()
    {
        super();
    }

    /**
     * Constructs a failure response in response to a request for the
     * specified oid.
     */
    public FailureResponse (int oid)
    {
        _oid = oid;
    }

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
        return "[type=FAIL, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    protected int _oid;
}
