//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Communicates failure to subscribe to an object.
 */
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
     * Constructs a failure response in response to a request for the specified oid.
     */
    public FailureResponse (int oid, String message)
    {
        _oid = oid;
        _message = message;
    }

    public int getOid ()
    {
        return _oid;
    }

    public String getMessage ()
    {
        return _message;
    }

    @Override
    public String toString ()
    {
        return "[type=FAIL, msgid=" + messageId + ", oid=" + _oid + ", msg=" + _message + "]";
    }

    protected int _oid;
    protected String _message;
}
