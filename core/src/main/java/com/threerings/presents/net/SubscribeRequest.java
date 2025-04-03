//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Requests to subscribe to a particular distributed object.
 */
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

    @Override
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
