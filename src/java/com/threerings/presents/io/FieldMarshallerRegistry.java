//
// $Id: FieldMarshallerRegistry.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.dobj.net;

import java.lang.reflect.Field;
import java.util.Hashtable;
import com.threerings.cocktail.cher.Log;

/**
 * Field marshaller instances are registered for each type of field that
 * is supported in a distributed object.
 */
public class FieldMarshallerRegistry
{
    /**
     * Returns a field marshaller that can marshall and unmarshall the
     * specified type of field. If no marshaller is registered for that
     * field type, an illegal argument exception will be thrown.
     * Distributed objects must not contain fields that cannot be
     * transmitted on the wire.
     */
    public static FieldMarshaller getMarshaller (Field field)
    {
        Class clazz = field.getType();
        FieldMarshaller marsh = (FieldMarshaller)_registry.get(clazz);
        if (marsh == null) {
            String errmsg = "No field marshaller registered for fields " +
                "of type " + clazz.getName() + ".";
            throw new IllegalArgumentException(errmsg);
        }
        return marsh;
    }

    /**
     * Registers the specified field marshaller as the one to use for
     * fields of the specified class. This assumes that the field
     * marshaller implementation provides a prototype field of the type it
     * knows how to marshall by the name of <code>prototype</code>.
     *
     * @param clazz the field marshaller class to be registered.
     */
    protected static void registerMarshaller (Class clazz)
    {
        try {
            _registry.put(clazz.getField("prototype").getType(),
                          clazz.newInstance());
        } catch (Exception e) {
            Log.warning("Unable to register field marshaller " +
                        "[class=" + clazz.getName() + "].");
            Log.logStackTrace(e);
        }
    }

    protected static Hashtable _registry = new Hashtable();

    static {
        // register our field marshallers
        registerMarshaller(BooleanFieldMarshaller.class);
        registerMarshaller(ShortFieldMarshaller.class);
        registerMarshaller(IntFieldMarshaller.class);
        registerMarshaller(LongFieldMarshaller.class);
        registerMarshaller(FloatFieldMarshaller.class);
        registerMarshaller(DoubleFieldMarshaller.class);
        registerMarshaller(StringFieldMarshaller.class);
    }
}
