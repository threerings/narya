//
// $Id: UnsubscribeResponse.java,v 1.1 2003/01/21 22:02:37 mdb Exp $

package com.threerings.presents.net;

/**
 * Used to communicate to the client that we received their unsubscribe
 * request and that it is now OK to remove an object mapping from their
 * local object table.
 */
public class UnsubscribeResponse extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UnsubscribeResponse ()
    {
        super();
    }

    /**
     * Constructs an unsubscribe response with the supplied oid.
     */
    public UnsubscribeResponse (int oid)
    {
        _oid = oid;
    }

    public int getOid ()
    {
        return _oid;
    }

    public String toString ()
    {
        return "[type=UNACK, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    protected int _oid;
}
