//
// $Id: EventNotification.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

public class EventNotification extends DownstreamMessage
{
    /** The code for an event notification. */
    public static final short TYPE = TYPE_BASE + 1;

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
    public EventNotification (Event event)
    {
        _event = event;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        _event.writeTo(out);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _event = EventFactory.readFrom(in);
    }

    /** The event which we are forwarding. */
    protected Event _event;
}
