//
// $Id: LongFieldMarshaller.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.dobj.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;

public class LongFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public long prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        out.writeLong(field.getLong(dobj));
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.setLong(dobj, in.readLong());
    }
}
