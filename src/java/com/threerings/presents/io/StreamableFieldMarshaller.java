//
// $Id: StreamableFieldMarshaller.java,v 1.1 2001/10/12 19:28:43 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.io.Streamable;

public class StreamableFieldMarshaller implements FieldMarshaller
{
    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        Streamable value = (Streamable)field.get(dobj);
        // we freak out if our streamable is null
        if (value == null) {
            throw new IllegalAccessException(
                "No streamable instance to marshall!");
        }
        value.writeTo(out);
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        try {
            // create a new instance into which to unmarshall the field
            Streamable value = (Streamable)field.getType().newInstance();
            // unserialize it
            value.readFrom(in);
            // and set the value in the object
            field.set(dobj, value);

        } catch (InstantiationException ie) {
            throw new IOException("Unable to instantiate streamable " +
                                  "[field=" + field + ", object=" + dobj +
                                  ", error=" + ie + "]");
        }
    }
}
