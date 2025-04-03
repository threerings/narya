//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Requests to end our session with the server.
 */
public class LogoffRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public LogoffRequest ()
    {
        super();
    }

    @Override
    public String toString ()
    {
        return "[type=LOGOFF, msgid=" + messageId + "]";
    }
}
