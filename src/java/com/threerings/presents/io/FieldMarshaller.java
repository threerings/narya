//
// $Id: FieldMarshaller.java,v 1.7 2002/07/17 23:05:28 mdb Exp $

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
    public Class getFieldType ();

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException;

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException;
}
