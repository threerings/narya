//
// $Id: ForwardEventRequest.java,v 1.6 2001/06/05 22:44:31 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.dobj.DEvent;

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
    public ForwardEventRequest (DEvent event)
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
    public DEvent getEvent ()
    {
        return _event;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        // _event.writeTo(out);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        // _event = EventFactory.readFrom(in);
    }

    /** The event which we are forwarding. */
    protected DEvent _event;
}
