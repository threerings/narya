//
// $Id: IntArrayFieldMarshaller.java,v 1.1 2001/10/17 02:56:51 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.DObject;

public class IntArrayFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public int[] prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        int[] value = (int[])field.get(dobj);
        // we convert null arrays to zero length arrays
        if (value == null) {
            out.writeInt(0);

        } else {
            out.writeInt(value.length);
            for (int i = 0; i < value.length; i++) {
                out.writeInt(value[i]);
            }
        }
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        int[] value = new int[in.readInt()];
        for (int i = 0; i < value.length; i++) {
            value[i] = in.readInt();
        }
        field.set(dobj, value);
    }
}
