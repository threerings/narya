//
// $Id: StreamableFieldMarshaller.java,v 1.6 2002/07/17 23:05:28 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.samskivert.io.NestableIOException;
import com.samskivert.util.StringUtil;

import com.threerings.presents.io.Streamable;

public class StreamableFieldMarshaller implements FieldMarshaller
{
    /** Returns the sort of field that we marshall. */
    public Class getFieldType ()
    {
        return Streamable.class;
    }

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        Streamable value = (Streamable)field.get(obj);
        if (value == null) {
            out.writeUTF("");
        } else {
            out.writeUTF(value.getClass().getName());
            value.writeTo(out);
        }
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        try {
            Streamable value = null;
            String scnm = in.readUTF();

            // instantiate and unserialize the streamable if we actually
            // have a value
            if (!StringUtil.blank(scnm)) {
                Class sclass = Class.forName(scnm);
                // create a new instance into which to unmarshall the field
                value = (Streamable)sclass.newInstance();
                // unserialize it
                value.readFrom(in);
            }

            // and set the value in the object
            field.set(obj, value);

        } catch (Exception e) {
            String errmsg = "Unable to instantiate streamable " +
                "[field=" + field + ", object=" + obj + "]";
            throw new NestableIOException(errmsg, e);
        }
    }
}
