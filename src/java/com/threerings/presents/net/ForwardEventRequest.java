//
// $Id: ForwardEventRequest.java,v 1.12 2002/12/20 23:41:26 mdb Exp $

package com.threerings.presents.net;

import com.threerings.presents.dobj.DEvent;

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

    public String toString ()
    {
        return "[type=FWD, evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected DEvent _event;
}
