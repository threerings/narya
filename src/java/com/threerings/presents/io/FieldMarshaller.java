//
// $Id: FieldMarshaller.java,v 1.6 2002/02/01 23:32:37 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * A field marshaller knows how to marshall and unmarshall a particular
 * data type. It is called upon to do the actual on the wire
 * encoding/decoding of a field.
 */
public interface FieldMarshaller
{
    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException;

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException;
}
