//
// $Id: IntegerFieldMarshaller.java,v 1.1 2002/04/17 22:45:32 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class IntegerFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public Integer prototype;

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        Integer value = (Integer)field.get(obj);
        // we convert null integers to zero
        out.writeInt((value == null) ? 0 : value.intValue());
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        field.set(obj, new Integer(in.readInt()));
    }
}
