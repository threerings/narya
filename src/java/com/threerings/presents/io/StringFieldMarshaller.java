//
// $Id: StringFieldMarshaller.java,v 1.7 2002/02/01 23:32:37 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class StringFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public String prototype;

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
