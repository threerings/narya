//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Notifies the server that the client has received its {@link UpdateThrottleMessage}.
 */
public class ThrottleUpdatedMessage extends UpstreamMessage
{
    /** The number of messages allowed per second. */
    public final int messagesPerSec;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public ThrottleUpdatedMessage ()
    {
        this.messagesPerSec = 0;
    }

    public ThrottleUpdatedMessage (int messagesPerSec)
    {
        this.messagesPerSec = messagesPerSec;
    }
}
