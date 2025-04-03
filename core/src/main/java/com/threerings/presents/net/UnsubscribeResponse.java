//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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

    @Override
    public String toString ()
    {
        return "[type=UNACK, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    protected int _oid;
}
