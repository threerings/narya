//
// $Id: EventNotification.java,v 1.13 2002/12/20 23:41:26 mdb Exp $

package com.threerings.presents.net;

import com.threerings.presents.dobj.DEvent;

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

    public String toString ()
    {
        return "[type=EVT, evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected DEvent _event;
}
