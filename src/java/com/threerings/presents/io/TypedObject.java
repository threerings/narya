//
// $Id: TypedObject.java,v 1.4 2001/08/16 03:25:14 mdb Exp $

package com.threerings.cocktail.cher.io;

/**
 * A typed object is one that is associated with a particular type code.
 * The type code can be communicated on the wire and used by the receiving
 * end to instantiate the proper typed object class for decoding (which is
 * done by the <code>TypedObjectFactory</code>).
 *
 * @see TypedObjectFactory
 */
public interface TypedObject extends Streamable
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
}
