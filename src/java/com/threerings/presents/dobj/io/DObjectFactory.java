//
// $Id: DObjectFactory.java,v 1.2 2001/05/30 00:16:00 mdb Exp $

package com.samskivert.cocktail.cher.dobj.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

import com.samskivert.cocktail.cher.Log;
import com.samskivert.cocktail.cher.dobj.DObject;
import com.samskivert.cocktail.cher.io.ObjectStreamException;

/**
 * The distributed object factory is responsible for marshalling and
 * unmarshalling distributed objects to and from streams so that they can
 * be communicated between the client and server.
 */
public class DObjectFactory
{
    /**
     * Writes the supplied distributed object out to the specified data
     * output stream.
     */
    public static void writeTo (DataOutputStream out, DObject dobj)
        throws IOException
    {
        Log.info("Marshalling object: " + dobj);
        // look up the marshaller for this object
        Class clazz = dobj.getClass();
        Marshaller marsh = getMarshaller(clazz);
        // then write the class of the object to the stream
        out.writeUTF(clazz.getName());
        // then use the marshaller to write the object itself
        marsh.writeTo(out, dobj);
    }

    /**
     * Reads a distributed object from the specified input stream.
     */
    public static DObject readFrom (DataInputStream in)
        throws IOException
    {
        try {
            // read in the class name and create an instance of that class
            Class clazz = Class.forName(in.readUTF());
            DObject dobj = (DObject)clazz.newInstance();
            Log.info("Unmarshalling object: " + dobj);

            // look up the marshaller for that class
            Marshaller marsh = getMarshaller(clazz);
            // use it to reconstitute the object from the stream
            marsh.readFrom(in, dobj);

            return dobj;

        } catch (Exception e) {
            String errmsg = "Unable to unserialize dobj: " + e;
            throw new ObjectStreamException(errmsg);
        }
    }

    protected static Marshaller getMarshaller (Class clazz)
        throws IOException
    {
        Marshaller marsh = (Marshaller)_marshallers.get(clazz);
        // create a new marshaller if we don't already have one
        if (marsh == null) {
            _marshallers.put(clazz, marsh = new Marshaller(clazz));
        }
        return marsh;
    }

    protected static HashMap _marshallers = new HashMap();
}
