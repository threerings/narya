//
// $Id: Streamable.java,v 1.1 2001/08/16 03:25:14 mdb Exp $

package com.threerings.cocktail.cher.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * A streamable is an object that knows how to read and write itself on a
 * stream. We are obliged to reimplement an object serialization framework
 * rather than use the one provided by Java because we're interested in
 * taking maximal advantage of situations where we know the types of
 * objects that are being streamed and in those cases we avoid writing
 * type information for the objects and only write the values. In other
 * cases, we assign unique codes to object classes (see {@link
 * TypedObject}) to minimize the network footprint of the serialized
 * objects.
 */
public interface Streamable
{
    /**
     * Writes all of the members of this object to the supplied data
     * output stream.
     */
    public void writeTo (DataOutputStream out)
        throws IOException;

    /**
     * Reads all of the members of this object in from the supplied data
     * input stream.
     */
    public void readFrom (DataInputStream in)
        throws IOException;
}
