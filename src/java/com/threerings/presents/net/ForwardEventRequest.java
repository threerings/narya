//
// $Id: ForwardEventRequest.java,v 1.9 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.dobj.TypedEvent;
import com.threerings.presents.io.TypedObjectFactory;

public class ForwardEventRequest extends UpstreamMessage
{
    /** The code for a forward event request. */
    public static final short TYPE = TYPE_BASE + 4;

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
    public ForwardEventRequest (TypedEvent event)
    {
        _event = event;
    }

    public short getType ()
    {
        return TYPE;
    }

    /**
     * Returns the event that we wish to have forwarded.
     */
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
        return "[type=FWD, msgid=" + messageId + ", evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected TypedEvent _event;
}
