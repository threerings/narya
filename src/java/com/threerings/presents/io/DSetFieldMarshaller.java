//
// $Id: DSetFieldMarshaller.java,v 1.1 2001/08/20 21:44:10 mdb Exp $

package com.threerings.cocktail.cher.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.dobj.DSet;

public class DSetFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public DSet prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        DSet value = (DSet)field.get(dobj);
        // we freak out if our set is null
        if (value == null) {
            throw new IllegalAccessException("No set instance to marshall!");
        }
        value.writeTo(out);
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        DSet value = new DSet();
        value.readFrom(in);
        field.set(dobj, value);
    }
}
