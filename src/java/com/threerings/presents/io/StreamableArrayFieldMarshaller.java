//
// $Id: StreamableArrayFieldMarshaller.java,v 1.1 2002/03/20 22:58:26 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.threerings.presents.io.Streamable;

public class StreamableArrayFieldMarshaller implements FieldMarshaller
{
    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        Streamable[] values = (Streamable[])field.get(obj);

        // in order to make use of the general purpose streamable
        // serialization routines, we have to have a non-null array, so we
        // create a zero length array if our field value is null
        if (values == null) {
            values = (Streamable[])Array.newInstance(
                field.getType().getComponentType(), 0);
        }

        StreamableUtil.writeStreamables(out, values);
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        // read and set
        field.set(obj, StreamableUtil.readStreamables(in));
    }
}
