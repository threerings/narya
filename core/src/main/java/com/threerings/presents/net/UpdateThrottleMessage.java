//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Notifies the client that its message throttle has been updated.
 */
public class UpdateThrottleMessage extends DownstreamMessage
{
    /** The number of messages allowed per second. */
    public final int messagesPerSec;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UpdateThrottleMessage ()
    {
        this.messagesPerSec = 0;
    }

    public UpdateThrottleMessage (int messagesPerSec)
    {
        this.messagesPerSec = messagesPerSec;
    }
}
