//
// $Id: ObjectResponse.java,v 1.5 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.dobj.net.DObjectFactory;

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
        DObjectFactory.writeTo(out, _dobj);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        messageId = in.readShort();
        _dobj = (DObject)DObjectFactory.readFrom(in);
    }

    /** The object which is associated with this response. */
    protected DObject _dobj;
}
