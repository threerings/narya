//
// $Id: LongFieldMarshaller.java,v 1.7 2002/07/17 23:05:28 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class LongFieldMarshaller implements FieldMarshaller
{
    /** Returns the sort of field that we marshall. */
    public Class getFieldType ()
    {
        return Long.TYPE;
    }

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        out.writeLong(field.getLong(obj));
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        field.setLong(obj, in.readLong());
    }
}
