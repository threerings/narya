//
// $Id: StringFieldMarshaller.java,v 1.3 2001/06/09 23:39:04 mdb Exp $

package com.threerings.cocktail.cher.dobj.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;

public class StringFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public String prototype;

    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        String value = (String)field.get(dobj);
        // we convert null strings to empty strings
        if (value == null) {
            value = "";
        }
        out.writeUTF(value);
    }

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException
    {
        field.set(dobj, in.readUTF());
    }
}
