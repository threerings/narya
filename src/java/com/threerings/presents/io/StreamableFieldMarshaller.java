//
// $Id: StreamableFieldMarshaller.java,v 1.4 2002/02/19 03:30:55 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.io.Streamable;

public class StreamableFieldMarshaller implements FieldMarshaller
{
    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        Streamable value = (Streamable)field.get(obj);
        // we freak out if our streamable is null
        if (value == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            value.writeTo(out);
        }
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        try {
            byte isNull = in.readByte();
            Streamable value = null;

            // instantiate and unserialize the streamable if we actually
            // have a value
            if (isNull == 1) {
                // create a new instance into which to unmarshall the field
                value = (Streamable)field.getType().newInstance();
                // unserialize it
                value.readFrom(in);
            }

            // and set the value in the object
            field.set(obj, value);

        } catch (InstantiationException ie) {
            throw new IOException("Unable to instantiate streamable " +
                                  "[field=" + field + ", object=" + obj +
                                  ", error=" + ie + "]");
        }
    }
}
