//
// $Id: FloatFieldMarshaller.java,v 1.1 2001/05/30 00:16:00 mdb Exp $

package com.samskivert.cocktail.cher.dobj.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.samskivert.cocktail.cher.dobj.DObject;

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
