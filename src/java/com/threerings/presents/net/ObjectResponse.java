//
// $Id: ObjectResponse.java,v 1.1 2001/05/22 21:51:29 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.dobj.DObject;
import com.samskivert.cocktail.cher.io.TypedObjectFactory;

public class ObjectResponse extends DownstreamMessage
{
    /** The code for an event notification. */
    public static final short TYPE = TYPE_BASE + 2;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public ObjectResponse ()
    {
        super();
    }

    /**
     * Constructs an object response with supplied distributed object that
     * is associated with the specified upstream message id.
     */
    public ObjectResponse (short messageId, DObject dobj)
    {
        this.messageId = messageId;
        _dobj = dobj;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeShort(messageId);
        _dobj.writeTo(out);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        messageId = in.readShort();
        _dobj = (DObject)TypedObjectFactory.readFrom(in);
    }

    /** The object which is associated with this response. */
    protected DObject _dobj;
}
