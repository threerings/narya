//
// $Id: ObjectResponse.java,v 1.13 2002/12/20 23:28:24 mdb Exp $

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

    public String toString ()
    {
        return "[type=ORSP, msgid=" + messageId + ", obj=" + _dobj + "]";
    }

    /** The object which is associated with this response. */
    protected DObject _dobj;
}
