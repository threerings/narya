//
// $Id: ShortFieldMarshaller.java,v 1.3 2001/06/11 17:42:20 mdb Exp $

package com.threerings.cocktail.cher.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;

public class ShortFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public short prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        out.writeShort(field.getShort(dobj));
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.setShort(dobj, in.readShort());
    }
}
