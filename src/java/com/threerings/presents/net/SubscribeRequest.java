//
// $Id: SubscribeRequest.java,v 1.7 2002/12/20 23:28:24 mdb Exp $

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
