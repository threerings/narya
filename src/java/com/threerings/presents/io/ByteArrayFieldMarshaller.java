//
// $Id: ByteArrayFieldMarshaller.java,v 1.1 2002/02/07 05:27:07 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;

public class ByteArrayFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public byte[] prototype;

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        byte[] value = (byte[])field.get(obj);
        // we convert null arrays to zero length arrays
        if (value == null) {
            out.writeInt(0);

        } else {
            out.writeInt(value.length);
            out.write(value);
        }
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        int vlength = in.readInt();
        byte[] value = new byte[vlength];
        if (in.read(value) != vlength) {
            throw new EOFException();
        }
        field.set(obj, value);
    }
}
