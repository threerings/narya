//
// $Id: UnsubscribeRequest.java,v 1.9 2002/12/20 23:41:26 mdb Exp $

package com.threerings.presents.net;

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
