//
// $Id: IntFieldMarshaller.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.dobj.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;

public class IntFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public int prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        out.writeInt(field.getInt(dobj));
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.setInt(dobj, in.readInt());
    }
}
