//
// $Id: IntegerFieldMarshaller.java,v 1.2 2002/07/17 23:05:28 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class IntegerFieldMarshaller implements FieldMarshaller
{
    /** Returns the sort of field that we marshall. */
    public Class getFieldType ()
    {
        return Integer.class;
    }

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
