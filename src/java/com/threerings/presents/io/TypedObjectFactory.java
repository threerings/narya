//
// $Id: TypedObjectFactory.java,v 1.6 2001/06/13 05:16:25 mdb Exp $

package com.threerings.cocktail.cher.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

import com.threerings.cocktail.cher.Log;

/**
 * The encodable factory is used to unserialize encodable object instances
 * from a stream. It maintains a mapping of encodable object classes to
 * identifier codes which it uses to determine what sort of object is on
 * the stream. The encodable mechanism is used to associate a particular
 * class with some compact code that can be transmitted on the wire to
 * identify that class.
 */
public class TypedObjectFactory
{
    /**
     * Reads (unserializes) a typed object from the supplied data input
     * stream.
     *
     * @return The unserialized typed object instance.
     */
    public static TypedObject readFrom (DataInputStream din)
        throws IOException, ObjectStreamException
    {
        // first determine the type of the incoming object
        short type = din.readShort();

        // now instantiate the proper object and decode the remainder
        TypedObject msg = newObjectByType(type);
        msg.readFrom(din);

        return msg;
    }

    /**
     * Writes (serializes) a typed object to the supplied data output
     * stream.
     */
    public static void writeTo (DataOutputStream dout, TypedObject tobj)
        throws IOException
    {
        // first write the type of the object
        dout.writeShort(tobj.getType());
        // then write the object itself
        tobj.writeTo(dout);
    }

    /**
     * Registers the supplied class with the specified type code. If a
     * class is already registered with that type code a runtime exception
     * will be thrown so that the proper freaking out can occur.
     */
    public static void registerClass (short type, Class clazz)
    {
        Short key = new Short(type);

        // make sure no funny business is afoot
        if (_classes.containsKey(key)) {
            Class incumbent = (Class)_classes.get(key);
            String errmsg = "Cannot register " + clazz.getName() +
                " as type " + type + " because " + incumbent.getName() +
                " is already registered with that type.";
            throw new RuntimeException(errmsg);
        }

        // set up the mapping
        _classes.put(key, clazz);
    }

    protected static TypedObject newObjectByType (short type)
        throws ObjectStreamException
    {
        Class clazz = (Class)_classes.get(new Short(type));
        if (clazz == null) {
            String errmsg = "Unknown object type: " + type;
            throw new ObjectStreamException(errmsg);
        }

        try {
            return (TypedObject)clazz.newInstance();
        } catch (Throwable t) {
            Log.warning("Typed object error: " + t);
            Log.logStackTrace(t);

            String errmsg = "Unable to instantiate typed object " +
                "[class=" + clazz.getName() +
                ", error=" + t.getMessage() + "].";
            throw new ObjectStreamException(errmsg);
        }
    }

    /** Our type to class mapping table. */
    protected static HashMap _classes = new HashMap();

    // register our typed object
    static {
        TypedObjectRegistry.registerTypedObjects();
    }
}
