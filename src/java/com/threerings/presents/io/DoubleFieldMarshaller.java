//
// $Id: DoubleFieldMarshaller.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.DObject;

public class DoubleFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public double prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        out.writeDouble(field.getDouble(dobj));
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.setDouble(dobj, in.readDouble());
    }
}
