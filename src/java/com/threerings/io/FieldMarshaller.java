//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.io;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

/**
 * Used to read and write a single field of a {@link Streamable} instance.
 */
public abstract class FieldMarshaller
{
    /**
     * Reads the contents of the supplied field from the supplied stream and sets it in the
     * supplied object.
     */
    public abstract void readField (Field field, Object target, ObjectInputStream in)
        throws Exception;

    /**
     * Writes the contents of the supplied field in the supplied object to the supplied stream.
     */
    public abstract void writeField (Field field, Object source, ObjectOutputStream out)
        throws Exception;

    /**
     * Returns a field marshaller appropriate for the supplied field or null if no marshaller
     * exists for the type contained by the field in question.
     */
    public static FieldMarshaller getFieldMarshaller (Field field)
    {
        if (_marshallers == null) {
            createMarshallers();
        }

        Class ftype = field.getType();
        if (ftype.isInterface()) {
            // if the class is a pure interface, use Object.
            ftype = Object.class;
        }

        // if we have an exact match, use that
        FieldMarshaller fm = _marshallers.get(ftype);

        // otherwise if the class is a streamable, use the streamable marshaller
        if (fm == null && Streamer.isStreamable(ftype)) {
            fm = _marshallers.get(Streamable.class);
        }

        return fm;
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

        // documentation inherited
        public void readField (Field field, Object target, ObjectInputStream in)
            throws Exception
        {
            if (in.readBoolean()) {
                Object value = _streamer.createObject(in);
                _streamer.readObject(value, in, true);
                field.set(target, value);
            } else {
                field.set(target, null);
            }
        }

        // documentation inherited
        public void writeField (Field field, Object source, ObjectOutputStream out)
            throws Exception
        {
            Object value = field.get(source);
            if (value == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                _streamer.writeObject(value, out, true);
            }
        }

        /** The streamer we use to read and write our field. */
        protected Streamer _streamer;
    }

    /**
     * Creates instances of all known field marshaller types and populates the {@link
     * #_marshallers} table with them. This is called the first time a marshaller is requested.
     */
    protected static void createMarshallers ()
    {
        // create our table
        _marshallers = new HashMap<Class,FieldMarshaller>();

        // create a generic marshaller for streamable instances
        FieldMarshaller gmarsh = new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                System.err.println("Reading generic");
                field.set(target, in.readObject());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeObject(field.get(source));
            }
        };
        _marshallers.put(Streamable.class, gmarsh);

        // use the same generic marshaller for fields declared to by type Object or Object[] with
        // the expectation that they will contain only primitive types or Streamables; the runtime
        // will fail informatively if we attempt to store non-Streamable objects in that field
        _marshallers.put(Object.class, gmarsh);
        _marshallers.put(Object[].class, gmarsh);

        // create marshallers for the primitive types
        _marshallers.put(Boolean.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setBoolean(target, in.readBoolean());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeBoolean(field.getBoolean(source));
            }
        });
        _marshallers.put(Byte.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setByte(target, in.readByte());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeByte(field.getByte(source));
            }
        });
        _marshallers.put(Character.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setChar(target, in.readChar());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeChar(field.getChar(source));
            }
        });
        _marshallers.put(Short.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setShort(target, in.readShort());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeShort(field.getShort(source));
            }
        });
        _marshallers.put(Integer.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setInt(target, in.readInt());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeInt(field.getInt(source));
            }
        });
        _marshallers.put(Long.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setLong(target, in.readLong());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeLong(field.getLong(source));
            }
        });
        _marshallers.put(Float.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setFloat(target, in.readFloat());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeFloat(field.getFloat(source));
            }
        });
        _marshallers.put(Double.TYPE, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.setDouble(target, in.readDouble());
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeDouble(field.getDouble(source));
            }
        });
        _marshallers.put(Date.class, new FieldMarshaller() {
            public void readField (Field field, Object target, ObjectInputStream in)
                throws Exception {
                field.set(target, new Date(in.readLong()));
            }
            public void writeField (Field field, Object source, ObjectOutputStream out)
                throws Exception {
                out.writeLong(((Date)field.get(source)).getTime());
            }
        });

        // create field marshallers for all of the basic types
        int bscount = BasicStreamers.BSTREAMER_TYPES.length;
        for (int ii = 0; ii < bscount; ii++) {
            _marshallers.put(BasicStreamers.BSTREAMER_TYPES[ii],
                             new StreamerMarshaller(BasicStreamers.BSTREAMER_INSTANCES[ii]));
        }
    }

    /** Contains a mapping from field type to field marshaller instance for that type. */
    protected static HashMap<Class,FieldMarshaller> _marshallers;
}
