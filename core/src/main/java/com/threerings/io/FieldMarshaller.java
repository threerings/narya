//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.ReflectPermission;

import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

import static com.threerings.NaryaLog.log;

/**
 * Used to read and write a single field of a {@link Streamable} instance, or a single component
 * of a streamable record.
 */
public abstract class FieldMarshaller
{
    public FieldMarshaller ()
    {
        this(null);
    }

    public FieldMarshaller (String type)
    {
        _type = type;
    }

    /**
     * Reads a value from the supplied stream, boxed if primitive. Record components are read this
     * way, as they are passed to the record's canonical constructor rather than set.
     */
    public abstract Object readValue (ObjectInputStream in)
        throws Exception;

    /**
     * Writes the supplied value, boxed if primitive, to the supplied stream.
     */
    public abstract void writeValue (Object value, ObjectOutputStream out)
        throws Exception;

    /**
     * Reads the contents of the supplied field from the supplied stream and sets it in the
     * supplied object.
     */
    public void readField (Field field, Object target, ObjectInputStream in)
        throws Exception
    {
        field.set(target, readValue(in));
    }

    /**
     * Writes the contents of the supplied field in the supplied object to the supplied stream.
     */
    public void writeField (Field field, Object source, ObjectOutputStream out)
        throws Exception
    {
        writeValue(field.get(source), out);
    }

    @Override
    public String toString ()
    {
        if (_type != null) {
            return "FieldMarshaller " + _type;
        }
        return super.toString();
    }

    protected final String _type;

    /**
     * Returns a field marshaller appropriate for the supplied field or null if no marshaller
     * exists for the type contained by the field in question.
     */
    public static FieldMarshaller getFieldMarshaller (Field field)
    {
        // if necessary (we're running in a sandbox), look for custom field accessors
        if (useFieldAccessors()) {
            Method reader = null, writer = null;
            try {
                reader = field.getDeclaringClass().getMethod(
                    getReaderMethodName(field.getName()), READER_ARGS);
            } catch (NoSuchMethodException nsme) {
                // no problem
            }
            try {
                writer = field.getDeclaringClass().getMethod(
                    getWriterMethodName(field.getName()), WRITER_ARGS);
            } catch (NoSuchMethodException nsme) {
                // no problem
            }
            if (reader != null && writer != null) {
                return new MethodFieldMarshaller(reader, writer);
            }
            if ((reader == null && writer != null) || (writer == null && reader != null)) {
                log.warning("Class contains one but not both custom field reader and writer",
                            "class", field.getDeclaringClass().getName(), "field", field.getName(),
                            "reader", reader, "writer", writer);
                // fall through to using reflection on the fields...
            }
        }

        return getMarshaller(field.getType(), field.isAnnotationPresent(Intern.class),
                             field.getDeclaringClass(), field.getName());
    }

    /**
     * Returns a marshaller appropriate for the supplied record component or null if no
     * marshaller exists for its type.
     */
    public static FieldMarshaller getComponentMarshaller (RecordComponent comp)
    {
        return getMarshaller(comp.getType(), comp.isAnnotationPresent(Intern.class),
                             comp.getDeclaringRecord(), comp.getName());
    }

    /**
     * Returns a marshaller for values of the supplied type or null if no marshaller exists for
     * that type.
     *
     * @param intern whether the values are {@link Intern}ed strings.
     * @param owner the class declaring the field or component, for logging.
     * @param name the name of the field or component, for logging.
     */
    protected static FieldMarshaller getMarshaller (
        Class<?> ftype, boolean intern, Class<?> owner, String name)
    {
        if (_marshallers == null) {
            // multiple threads may attempt to create the stock marshallers, but they'll just do
            // extra work and _marshallers will only ever contain a fully populated table
            _marshallers = createMarshallers();
        }

        // use the intern marshaller for pooled strings
        if (ftype == String.class && intern) {
            return _internMarshaller;
        }

        // if we have an exact match, use that
        FieldMarshaller fm = _marshallers.get(ftype);
        if (fm == null) {
            Class<?> collClass = Streamer.getCollectionClass(ftype);
            if (collClass != null && !collClass.equals(ftype)) {
                log.warning("Specific field types are discouraged " +
                    "for Iterables/Collections and Maps. The implementation type may not be " +
                    "recreated on the other side.",
                    "class", owner, "field", name,
                    "type", ftype, "shouldBe", collClass);
                fm = _marshallers.get(collClass);
            }

            // otherwise if the class is a pure interface or streamable,
            // use the streamable marshaller
            if (fm == null && (ftype.isInterface() || Streamer.isStreamable(ftype))) {
                fm = _marshallers.get(Streamable.class);
            }
        }

        return fm;
    }

    /**
     * Returns the name of the custom reader method which will be used if it exists to stream a
     * field with the supplied name.
     */
    public static final String getReaderMethodName (String field)
    {
        return "readField_" + field;
    }

    /**
     * Returns the name of the custom writer method which will be used if it exists to stream a
     * field with the supplied name.
     */
    public static final String getWriterMethodName (String field)
    {
        return "writeField_" + field;
    }

    /**
     * Returns true if we should use the generated field marshaller methods that allow us to work
     * around our inability to read and write protected and private fields of a {@link Streamable}.
     */
    protected static boolean useFieldAccessors ()
    {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
            return false;
        } catch (SecurityException se) {
            return true;
        }
    }

    /**
     * Used to marshall and unmarshall classes for which we have a basic {@link Streamer}.
     */
    protected static class StreamerMarshaller extends FieldMarshaller
    {
        public StreamerMarshaller (Streamer streamer)
        {
            _streamer = streamer;
        }

        @Override
        public Object readValue (ObjectInputStream in)
            throws Exception
        {
            if (in.readBoolean()) {
                Object value = _streamer.createObject(in);
                _streamer.readObject(value, in, true);
                return value;
            } else {
                return null;
            }
        }

        @Override
        public void writeValue (Object value, ObjectOutputStream out)
            throws Exception
        {
            if (value == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                _streamer.writeObject(value, out, true);
            }
        }

        @Override
        public String toString ()
        {
            return "StreamerMarshaller:" + _streamer.toString();
        }

        /** The streamer we use to read and write our field. */
        protected Streamer _streamer;
    }

    /**
     * Uses custom accessor methods to read and write a field.
     */
    protected static class MethodFieldMarshaller extends FieldMarshaller
    {
        public MethodFieldMarshaller (Method reader, Method writer)
        {
            _reader = reader;
            _writer = writer;
        }

        @Override
        public void readField (Field field, Object target, ObjectInputStream in)
            throws Exception
        {
            _reader.invoke(target, in);
        }

        @Override
        public void writeField (Field field, Object source, ObjectOutputStream out)
            throws Exception
        {
            _writer.invoke(source, out);
        }

        // the custom accessors read into and write from an instance, never a bare value
        @Override
        public Object readValue (ObjectInputStream in)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeValue (Object value, ObjectOutputStream out)
        {
            throw new UnsupportedOperationException();
        }

        protected Method _reader, _writer;
    }

    /**
     * Creates and returns a mapping for all known field marshaller types.
     */
    protected static Map<Class<?>, FieldMarshaller> createMarshallers ()
    {
        Map<Class<?>, FieldMarshaller> marshallers = Maps.newHashMap();

        // create a generic marshaller for streamable instances
        FieldMarshaller gmarsh = new FieldMarshaller("Generic") {
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readObject();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeObject(value);
            }
        };
        marshallers.put(Streamable.class, gmarsh);

        // use the same generic marshaller for fields declared as Object with the expectation that
        // they will contain only primitive types or Streamables; the runtime will fail
        // informatively if we attempt to store non-Streamable objects in that field
        marshallers.put(Object.class, gmarsh);

        // create marshallers for the primitive types, which override the field methods to avoid
        // boxing
        marshallers.put(Boolean.TYPE, new FieldMarshaller("boolean") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setBoolean(target, in.readBoolean());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeBoolean(field.getBoolean(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readBoolean();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeBoolean((Boolean)value);
            }
        });
        marshallers.put(Byte.TYPE, new FieldMarshaller("byte") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setByte(target, in.readByte());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeByte(field.getByte(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readByte();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeByte((Byte)value);
            }
        });
        marshallers.put(Character.TYPE, new FieldMarshaller("char") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setChar(target, in.readChar());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeChar(field.getChar(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readChar();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeChar((Character)value);
            }
        });
        marshallers.put(Short.TYPE, new FieldMarshaller("short") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setShort(target, in.readShort());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeShort(field.getShort(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readShort();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeShort((Short)value);
            }
        });
        marshallers.put(Integer.TYPE, new FieldMarshaller("int") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setInt(target, in.readInt());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeInt(field.getInt(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readInt();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeInt((Integer)value);
            }
        });
        marshallers.put(Long.TYPE, new FieldMarshaller("long") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setLong(target, in.readLong());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeLong(field.getLong(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readLong();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeLong((Long)value);
            }
        });
        marshallers.put(Float.TYPE, new FieldMarshaller("float") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setFloat(target, in.readFloat());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeFloat(field.getFloat(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readFloat();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeFloat((Float)value);
            }
        });
        marshallers.put(Double.TYPE, new FieldMarshaller("double") {
            @Override
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setDouble(target, in.readDouble());
            }
            @Override
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeDouble(field.getDouble(source));
            }
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readDouble();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeDouble((Double)value);
            }
        });
        marshallers.put(Date.class, new FieldMarshaller("Date") {
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return new Date(in.readLong());
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeLong(((Date)value).getTime());
            }
        });

        // create field marshallers for all of the basic types
        for (Map.Entry<Class<?>,Streamer> entry : BasicStreamers.BSTREAMERS.entrySet()) {
            marshallers.put(entry.getKey(), new StreamerMarshaller(entry.getValue()));
        }

        // create the field marshaller for pooled strings
        _internMarshaller = new FieldMarshaller("intern") {
            @Override
            public Object readValue (ObjectInputStream in)
                throws Exception {
                return in.readIntern();
            }
            @Override
            public void writeValue (Object value, ObjectOutputStream out)
                throws Exception {
                out.writeIntern((String)value);
            }
        };

        return marshallers;
    }

    /** Contains a mapping from field type to field marshaller instance for that type. */
    protected static Map<Class<?>, FieldMarshaller> _marshallers;

    /** The field marshaller for pooled strings. */
    protected static FieldMarshaller _internMarshaller;

    /** Defines the signature to a custom field reader method. */
    protected static final Class<?>[] READER_ARGS = { ObjectInputStream.class };

    /** Defines the signature to a custom field writer method. */
    protected static final Class<?>[] WRITER_ARGS = { ObjectOutputStream.class };
}
