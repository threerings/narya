//
// $Id: ObjectResponse.java,v 1.11 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.io.DObjectFactory;

public class ObjectResponse extends DownstreamMessage
{
    /** The code for an object repsonse. */
    public static final short TYPE = TYPE_BASE + 3;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public ObjectResponse ()
    {
        super();
    }

    /**
     * Constructs an object response with the supplied distributed object.
     */
    public ObjectResponse (DObject dobj)
    {
        _dobj = dobj;
    }

    public short getType ()
    {
        return TYPE;
    }

    public DObject getObject ()
    {
        return _dobj;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        DObjectFactory.writeTo(out, _dobj);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _dobj = (DObject)DObjectFactory.readFrom(in);
    }

    public String toString ()
    {
        return "[type=ORSP, msgid=" + messageId + ", obj=" + _dobj + "]";
    }

    /** The object which is associated with this response. */
    protected DObject _dobj;
}
