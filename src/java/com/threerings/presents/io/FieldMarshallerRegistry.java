//
// $Id: FieldMarshallerRegistry.java,v 1.13 2002/02/07 05:27:07 mdb Exp $

package com.threerings.presents.io;

import java.lang.reflect.Field;
import java.util.Hashtable;

import com.threerings.presents.Log;
import com.threerings.presents.io.Streamable;

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

        // if we don't have a marshaller registered for this specific
        // class, see if the class is an instance of streamable in which
        // case we stick the streamable marshaller into the registry for
        // this class
        if (marsh == null) {
            if (Streamable.class.isAssignableFrom(clazz)) {
                marsh = _streamableMarsh;
                _registry.put(clazz, marsh);
            }
        }

        // if we still don't have a marshaller, we're out of luck
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

    /** A mapping of classes to marshallers. */
    protected static Hashtable _registry = new Hashtable();

    /** Used for marshalling instances of {@link Streamable}. */
    protected static FieldMarshaller _streamableMarsh =
        new StreamableFieldMarshaller();

    static {
        // register our field marshallers
        registerMarshaller(BooleanFieldMarshaller.class);
        registerMarshaller(ByteFieldMarshaller.class);
        registerMarshaller(ShortFieldMarshaller.class);
        registerMarshaller(IntFieldMarshaller.class);
        registerMarshaller(LongFieldMarshaller.class);
        registerMarshaller(FloatFieldMarshaller.class);
        registerMarshaller(DoubleFieldMarshaller.class);
        registerMarshaller(StringFieldMarshaller.class);
        registerMarshaller(ByteArrayFieldMarshaller.class);
        registerMarshaller(IntArrayFieldMarshaller.class);
        registerMarshaller(StringArrayFieldMarshaller.class);
        registerMarshaller(OidListFieldMarshaller.class);
        registerMarshaller(DSetFieldMarshaller.class);
    }
}
