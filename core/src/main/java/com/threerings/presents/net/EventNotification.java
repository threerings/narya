//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import com.threerings.presents.dobj.DEvent;

/**
 * Contains an event forwarded from the server.
 */
public class EventNotification extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public EventNotification ()
    {
        super();
    }

    /**
     * Constructs an event notification for the supplied event.
     */
    public EventNotification (DEvent event)
    {
        _event = event;
    }

    public DEvent getEvent ()
    {
        return _event;
    }

    @Override
    public void setTransport (Transport transport)
    {
        // the event handles the transport
        _event.setTransport(transport);
    }

    @Override
    public Transport getTransport ()
    {
        return _event.getTransport();
    }

    @Override
    public void noteActualTransport (Transport transport)
    {
        _event.noteActualTransport(transport);
    }

    @Override
    public String toString ()
    {
        return "[type=EVT, evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected DEvent _event;
}
