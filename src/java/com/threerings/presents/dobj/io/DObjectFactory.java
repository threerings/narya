//
// $Id: DObjectFactory.java,v 1.1 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.dobj;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.io.ObjectStreamException;

/**
 * The distributed object factory is responsible for marshalling and
 * unmarshalling distributed objects to and from streams so that they can
 * be communicated between the client and server.
 */
public class DObjectFactory
{
    public static void writeTo (DataOutputStream out, DObject dobj)
        throws IOException
    {
        // first we write the class of the object to the stream
        out.writeUTF(dobj.getClass().getName());
        // then we write the object itself
        dobj.writeTo(out);
    }

    public static DObject readFrom (DataInputStream in)
        throws IOException
    {
        try {
            Class clazz = Class.forName(in.readUTF());
            DObject dobj = (DObject)clazz.newInstance();
            dobj.readFrom(in);
            return dobj;

        } catch (Exception e) {
            String errmsg = "Unable to unserialize dobj: " + e;
            throw new ObjectStreamException(errmsg);
        }
    }
}
