//
// $Id: ByteFieldMarshaller.java,v 1.1 2002/02/03 06:06:10 shaper Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ByteFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public byte prototype;

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        out.writeByte(field.getByte(obj));
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        field.setByte(obj, in.readByte());
    }
}
