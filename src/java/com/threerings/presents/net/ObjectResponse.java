//
// $Id: ObjectResponse.java,v 1.12 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DObject;

public class ObjectResponse extends DownstreamMessage
{
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

    public DObject getObject ()
    {
        return _dobj;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeObject(_dobj);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _dobj = (DObject)in.readObject();
    }

    public String toString ()
    {
        return "[type=ORSP, msgid=" + messageId + ", obj=" + _dobj + "]";
    }

    /** The object which is associated with this response. */
    protected DObject _dobj;
}
