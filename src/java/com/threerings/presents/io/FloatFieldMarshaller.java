//
// $Id: FloatFieldMarshaller.java,v 1.5 2002/02/01 23:26:49 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class FloatFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public float prototype;

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        out.writeFloat(field.getFloat(obj));
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        field.setFloat(obj, in.readFloat());
    }
}
