//
// $Id: TypedEvent.java,v 1.2 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.threerings.presents.io.TypedObject;

/**
 * A typed event is one that can be transmitted over the network. All
 * event classes that will be shared between the client and server should
 * derive from this class and be registered with the typed object factory
 * so that they can be serialized and unserialized.
 */
public abstract class TypedEvent extends DEvent implements TypedObject
{
    /**
     * All event derived classes should base their typed object code on
     * this base value.
     */
    public static final short TYPE_BASE = 400;

    /**
     * Constructs a typed event, passing the target object id on to the
     * <code>DEvent</code> constructor.
     */
    public TypedEvent (int targetOid)
    {
        super(targetOid);
    }

    /**
     * Constructs a blank typed event instance that will be unserialized
     * from the network.
     */
    public TypedEvent ()
    {
        super(0);
    }

    /**
     * Derived classes should override this function to write their fields
     * out to the supplied data output stream. They <em>must</em> be sure
     * to first call <code>super.writeTo()</code>.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(_toid);
    }

    /**
     * Derived classes should override this function to read their fields
     * from the supplied data input stream. They <em>must</em> be sure to
     * first call <code>super.readFrom()</code>.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        _toid = in.readInt();
    }
}
