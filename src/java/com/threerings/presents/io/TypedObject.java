//
// $Id: TypedObject.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * A typed object is one that is associated with a particular type code.
 * The type code can be communicated on the wire and used by the receiving
 * end to instantiate the proper typed object class for decoding.
 */
public abstract class TypedObject
{
    /**
     * Each typed object class must associate itself with a type value via
     * the <code>TypedObjectFactory</code> so that the factory can
     * instantiate the proper <code>TypedObject</code> derived class when
     * decoding an incoming message.
     *
     * @return The type code associated with this object.
     */
    public abstract short getType ();

    /**
     * Each typed object class must be able to write itself to a stream.
     * It should first call <code>super.writeTo()</code> before writing
     * its own fields to the stream.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeShort(getType());
    }

    /**
     * Each typed object class must be able to read itself from a stream.
     * It should first call <code>super.readFrom()</code> before reading
     * its own fields from the stream.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // nothing to do because the TypedObjectFactory already read our
        // type value from the stream
    }
}
