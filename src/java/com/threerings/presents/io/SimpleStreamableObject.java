//
// $Id: SimpleStreamableObject.java,v 1.1 2002/02/02 08:48:03 mdb Exp $

package com.threerings.presents.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.util.StringUtil;

/**
 * A simple streamable object uses the {@link Marshaller} class to stream
 * its fields and provides a simple mechanism for objects to make
 * themselves streamable without having to write any code. They simply
 * declare all fields to be streamed as public data members (marking
 * public fields as <code>transient</code> that should not be streamed)
 * and ensure that the fields are all valid streamable types (see {@link
 * FieldMarshallerRegistry}).
 */
public class SimpleStreamableObject
    implements Streamable
{
    // documentation inherited from interface
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        Marshaller.writeObject(out, this);
    }

    // documentation inherited from interface
    public void readFrom (DataInputStream in)
        throws IOException
    {
        Marshaller.readObject(in, this);
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
