//
// $Id: OidListFieldMarshaller.java,v 1.3 2002/02/01 23:26:49 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.presents.dobj.OidList;

public class OidListFieldMarshaller implements FieldMarshaller
{
    /** This is the sort of field that we marshall. */
    public OidList prototype;

    public void writeTo (DataOutputStream out, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        OidList value = (OidList)field.get(obj);
        // we convert null oid lists to empty oid lists
        if (value == null) {
            value = new OidList();
        }
        value.writeTo(out);
    }

    public void readFrom (DataInputStream in, Field field, Object obj)
        throws IOException, IllegalAccessException
    {
        OidList value = new OidList();
        value.readFrom(in);
        field.set(obj, value);
    }
}
