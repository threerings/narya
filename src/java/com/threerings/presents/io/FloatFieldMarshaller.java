//
// $Id: FloatFieldMarshaller.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.DObject;

public class FloatFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public float prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        out.writeFloat(field.getFloat(dobj));
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.setFloat(dobj, in.readFloat());
    }
}
