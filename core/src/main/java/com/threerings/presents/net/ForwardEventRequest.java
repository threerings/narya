//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import com.threerings.presents.dobj.DEvent;

/**
 * Forwards an event to the server for dispatch.
 */
public class ForwardEventRequest extends UpstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public ForwardEventRequest ()
    {
        super();
    }

    /**
     * Constructs a forward event request for the supplied event.
     */
    public ForwardEventRequest (DEvent event)
    {
        _event = event;
    }

    /**
     * Returns the event that we wish to have forwarded.
     */
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
    public String toString ()
    {
        return "[type=FWD, evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected DEvent _event;
}
