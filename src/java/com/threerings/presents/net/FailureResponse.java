//
// $Id: FailureResponse.java,v 1.10 2002/12/20 23:41:26 mdb Exp $

package com.threerings.presents.net;

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

    public String toString ()
    {
        return "[type=FAIL, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    protected int _oid;
}
