//
// $Id: EventNotification.java,v 1.9 2001/07/19 19:30:14 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.dobj.TypedEvent;
import com.threerings.cocktail.cher.io.TypedObjectFactory;

public class EventNotification extends DownstreamMessage
{
    /** The code for an event notification. */
    public static final short TYPE = TYPE_BASE + 2;

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
    public EventNotification (TypedEvent event)
    {
        _event = event;
    }

    public short getType ()
    {
        return TYPE;
    }

    public TypedEvent getEvent ()
    {
        return _event;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        // write the event out to the stream
        TypedObjectFactory.writeTo(out, _event);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        // read the event in from the stream
        _event = (TypedEvent)TypedObjectFactory.readFrom(in);
    }

    public String toString ()
    {
        return "[type=EVT, msgid=" + messageId + ", evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected TypedEvent _event;
}
