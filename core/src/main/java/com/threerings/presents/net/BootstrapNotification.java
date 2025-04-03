//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * A bootstrap notification is delivered to the client once the server has
 * fully initialized itself in preparation for dealing with this client.
 * The authentication process completes very early and further information
 * need be communicated to the client so that it can fully interact with
 * the server. This information is communicated via the bootstrap
 * notification.
 */
public class BootstrapNotification extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public BootstrapNotification ()
    {
        super();
    }

    /**
     * Constructs an bootstrap notification with the supplied data.
     */
    public BootstrapNotification (BootstrapData data)
    {
        _data = data;
    }

    public BootstrapData getData ()
    {
        return _data;
    }

    @Override
    public String toString ()
    {
        return "[type=BOOT, msgid=" + messageId + ", data=" + _data + "]";
    }

    /** The data associated with this notification. */
    protected BootstrapData _data;
}
