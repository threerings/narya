//
// $Id: TypedObject.java,v 1.3 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * A typed object is one that is associated with a particular type code.
 * The type code can be communicated on the wire and used by the receiving
 * end to instantiate the proper typed object class for decoding (which is
 * done by the <code>TypedObjectFactory</code>).
 *
 * @see TypedObjectFactory
 */
public interface TypedObject
{
    /**
     * Each typed object class must associate itself with a type value via
     * the <code>TypedObjectFactory</code> so that the factory can
     * instantiate the proper <code>TypedObject</code> derived class when
     * decoding an incoming message.
     *
     * @return The type code associated with this object.
     */
    public short getType ();

    /**
     * Each typed object class must be able to write itself to a stream.
     */
    public void writeTo (DataOutputStream out)
        throws IOException;

    /**
     * Each typed object class must be able to read itself from a stream.
     */
    public void readFrom (DataInputStream in)
        throws IOException;
}
