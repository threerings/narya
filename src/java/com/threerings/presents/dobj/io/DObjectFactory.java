//
// $Id: DObjectFactory.java,v 1.10 2002/02/01 23:26:49 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.io.ObjectStreamException;

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
        // Log.info("Marshalling object: " + dobj);

        // write the class of the object to the stream
        out.writeUTF(dobj.getClass().getName());
        // write out the oid
        out.writeInt(dobj.getOid());
        // then use the marshaller to write the object itself
        Marshaller.writeObject(out, dobj);
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
            dobj.setOid(in.readInt()); // read and set the oid

            // Log.info("Unmarshalling object: " + dobj);

            // use a marshaller to reconstitute the object from the stream
            Marshaller.readObject(in, dobj);

            return dobj;

        } catch (Exception e) {
            String errmsg = "Failure unserializing dobj";
            throw new ObjectStreamException(errmsg, e);
        }
    }
}
