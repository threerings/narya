//
// $Id: FieldMarshaller.java,v 1.3 2001/06/11 17:42:20 mdb Exp $

package com.threerings.cocktail.cher.dobj.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * A field marshaller knows how to marshall and unmarshall a particular
 * data type. It is called upon to do the actual on the wire
 * encoding/decoding of a field.
 */
public interface FieldMarshaller
{
    public void writeTo (DataOutputStream out, Field field, DObject dobj)
        throws IOException, IllegalAccessException;

    public void readFrom (DataInputStream in, Field field, DObject dobj)
        throws IOException, IllegalAccessException;
}
