//
// $Id: DSetFieldMarshaller.java,v 1.5 2002/07/17 23:05:28 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.DSet;

public class DSetFieldMarshaller implements FieldMarshaller
{
    /** Returns the sort of field that we marshall. */
    public Class getFieldType ()
    {
        return DSet.class;
    }

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        DSet value = (DSet)field.get(obj);
        // we freak out if our set is null
        if (value == null) {
            throw new IllegalAccessException("No set instance to marshall!");
        }
        value.writeTo(out);
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        DSet value = new DSet();
        value.readFrom(in);
        field.set(obj, value);
    }
}
