//
// $Id: StringFieldMarshaller.java,v 1.8 2002/07/17 23:05:28 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class StringFieldMarshaller implements FieldMarshaller
{
    /** Returns the sort of field that we marshall. */
    public Class getFieldType ()
    {
        return String.class;
    }

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        String value = (String)field.get(obj);
        // we convert null strings to empty strings
        if (value == null) {
            value = "";
        }
        out.writeUTF(value);
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        field.set(obj, in.readUTF());
    }
}
