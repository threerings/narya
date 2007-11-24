//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import com.samskivert.util.ClassUtil;
import com.samskivert.util.StringUtil;

import static com.threerings.NaryaLog.log;

/**
 * Handles the streaming of {@link Streamable} instances as well as a set of basic object types
 * (see {@link ObjectOutputStream}). An instance of {@link Streamer} is created for each distinct
 * class that implements {@link Streamable}. The {@link Streamer} reflects on the streamed class
 * and caches the information necessary to efficiently read and write objects of the class in
 * question.
 */
public class Streamer
{
    /**
     * Returns true if the supplied target class can be streamed using a streamer.
     */
    public synchronized static boolean isStreamable (Class target)
    {
        while (true) {
            // if we've got a streamer for it, it's good
            if (_streamers.containsKey(target)) {
                return true;
            }

            // enums can be streamed
            if (target.isEnum()) {
                return true;
            }

            // if it's not an array, it must be streamable
            if (!target.isArray()) {
                return Streamable.class.isAssignableFrom(target);
            }

            // otherwise extract the component type and loop back around for another check...
            target = target.getComponentType();
        }
    }

    /**
     * Returns the class that should be used when streaming this object. In general that is the
     * object's natural class, but for enum values, that might be its declaring class as enums use
     * classes in a way that would otherwise pollute our id to class mapping space.
     */
    public static Class getStreamerClass (Object object)
    {
        return (object instanceof Enum) ?
            ((Enum)object).getDeclaringClass() : object.getClass();
    }

    /**
     * Obtains a {@link Streamer} that can be used to read and write objects of the specified
     * target class. {@link Streamer} instances are shared among all {@link ObjectInputStream}s and
     * {@link ObjectOutputStream}s.
     *
     * @param target the class that is desired to be streamed. This should be the result of a call
     * to {@link #getStreamerClass} if the caller has an instance they wish to stream.
     *
     * @throws IOException when a streamer is requested for an object that does not implement
     * {@link Streamable} and is not one of the basic object types (@see {@link
     * ObjectOutputStream}).
     */
    public synchronized static Streamer getStreamer (final Class target)
        throws IOException
    {
        // if we have not yet initialized ourselves, do so now
        if (_streamers == null) {
            createStreamers();
        }

        Streamer stream = _streamers.get(target);
        if (stream == null) {
            // make sure this is a streamable class
            if (!isStreamable(target)) {
                Thread.dumpStack();
                throw new IOException("Requested to stream invalid class '" +
                                      target.getName() + "'");
            }

            // create a streamer for this class and cache it
            if (ObjectInputStream.STREAM_DEBUG) {
                log.info("Creating a streamer for '" + target.getName() + "'.");
            }

            // create our streamer in a privileged block so that it can introspect on the to be
            // streamed class
            try {
                stream = AccessController.doPrivileged(new PrivilegedExceptionAction<Streamer>() {
                    public Streamer run () throws IOException {
                        return new Streamer(target);
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getCause();
            }
            _streamers.put(target, stream);
        }
        return stream;
    }

    /**
     * Writes the supplied object to the specified stream.
     *
     * @param object the instance to be written to the stream.
     * @param out the stream to which to write the instance.
     * @param useWriter whether or not to use the custom <code>writeObject</code> if one exists.
     */
    public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
        throws IOException
    {
        // if we're supposed to and one exists, use the writer method
        if (useWriter && _writer != null) {
            try {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info("Writing with writer [class=" + _target.getName() + "].");
                }
                _writer.invoke(object, new Object[] { out });

            } catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException)t).getTargetException();
                }
                if (t instanceof IOException) {
                    throw (IOException)t;
                }
                String errmsg = "Failure invoking streamable writer " +
                    "[class=" + _target.getName() + "]";
                throw (IOException) new IOException(errmsg).initCause(t);
            }
            return;
        }

        // if we're writing an array, do some special business
        if (_target.isArray()) {
            int length = Array.getLength(object);
            out.writeInt(length);

            // if the component class is final, we can be sure that all instances in the array will
            // be of the same class and thus can serialize things more efficiently
            int cmods = _target.getComponentType().getModifiers();
            if (Modifier.isFinal(cmods)) {
                // compute a mask indicating which elements are null and which are populated
                ArrayMask mask = new ArrayMask(length);
                for (int ii = 0; ii < length; ii++) {
                    if (Array.get(object, ii) != null) {
                        mask.set(ii);
                    }
                }
                // write that mask out to the stream
                mask.writeTo(out);

                // now write out the populated elements
                for (int ii = 0; ii < length; ii++) {
                    Object element = Array.get(object, ii);
                    if (element != null) {
                        out.writeBareObject(element, _delegate, useWriter);
                    }
                }

            } else {
                // otherwise we've got to write each array element with its own class identifier
                // because it could be any derived class of the array element type
                for (int ii = 0; ii < length; ii++) {
                    out.writeObject(Array.get(object, ii));
                }
            }
            return;
        }

        // if we're writing an enum; write its string value (to avoid future compatibility issues
        // if someone serializes an enum to a file and then adds a value to the enum, changing the
        // ordinal assignments)
        if (_target.isEnum()) {
            out.writeUTF(((Enum)object).name());
            return;
        }

        // otherwise simply write out the fields via our field marshallers
        int fcount = _fields.length;
        for (int ii = 0; ii < fcount; ii++) {
            Field field = _fields[ii];
            FieldMarshaller fm = _marshallers[ii];
            if (fm == null) {
                String errmsg = "Unable to marshall field [class=" + _target.getName() +
                    ", field=" + field.getName() + ", type=" + field.getType().getName() + "]";
                throw new IOException(errmsg);
            }
            try {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info("Writing field [class=" + _target.getName() +
                             ", field=" + field.getName() + "].");
                }
                fm.writeField(field, object, out);
            } catch (Exception e) {
                String errmsg = "Failure writing streamable field [class=" + _target.getName() +
                    ", field=" + field.getName() + "]";
                throw (IOException) new IOException(errmsg).initCause(e);
            }
        }
    }

    /**
     * Creates a blank object that can subsequently be read by this streamer.  Data may be read
     * from the input stream as a result of this method (in the case of arrays, the length of the
     * array must be read before creating the array).
     */
    public Object createObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        try {
            // if our target class is an array type, read in the element count and create an array
            // instance of the appropriate type and size
            if (_target.isArray()) {
                int length = in.readInt();
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info(in.hashCode() + ": Creating array '" +
                             _target.getComponentType().getName() + "[" + length + "]'.");
                }
                return Array.newInstance(_target.getComponentType(), length);

            } else if (_target.isEnum()) {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info(in.hashCode() + ": Creating enum '" + _target.getName() + "'.");
                }
                @SuppressWarnings("unchecked") Object value = Enum.valueOf(_target, in.readUTF());
                return value;

            } else {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info(in.hashCode() + ": Creating object '" + _target.getName() + "'.");
                }
                return _target.newInstance();
            }

        } catch (InstantiationException ie) {
            String errmsg = "Error instantiating object [type=" + _target.getName() + "]";
            throw (IOException) new IOException(errmsg).initCause(ie);

        } catch (IllegalAccessException iae) {
            String errmsg = "Error instantiating object [type=" + _target.getName() + "]";
            throw (IOException) new IOException(errmsg).initCause(iae);
        }
    }

    /**
     * Reads and populates the fields of the supplied object from the specified stream.
     *
     * @param object the instance to be read from the stream.
     * @param in the stream from which to read the instance.
     * @param useReader whether or not to use the custom <code>readObject</code> if one exists.
     */
    public void readObject (Object object, ObjectInputStream in, boolean useReader)
        throws IOException, ClassNotFoundException
    {
        // if we're supposed to and one exists, use the reader method
        if (useReader && _reader != null) {
            try {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info(in.hashCode() + ": Reading with reader '" + _target.getName() + "." +
                             _reader.getName() + "()'.");
                }
                _reader.invoke(object, new Object[] { in });

            } catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException)t).getTargetException();
                }
                if (t instanceof IOException) {
                    throw (IOException)t;
                }
                String errmsg = "Failure invoking streamable reader " +
                    "[class=" + _target.getName() + "]";
                throw (IOException) new IOException(errmsg).initCause(t);
            }
            return;
        }

        if (ObjectInputStream.STREAM_DEBUG) {
            log.info(in.hashCode() + ": Reading '" + _target.getName() + "'.");
        }

        // if we're reading in an array, do some special business
        if (_target.isArray()) {
            int length = Array.getLength(object);
            // if the component class is final, we can be sure that all instances in the array will
            // be of the same class and thus have serialized things more efficiently
            int cmods = _target.getComponentType().getModifiers();
            if (Modifier.isFinal(cmods)) {
                // read in the nullness mask
                ArrayMask mask = new ArrayMask();
                mask.readFrom(in);
                // now read in the array elements given that we know which elements to read
                for (int ii = 0; ii < length; ii++) {
                    if (mask.isSet(ii)) {
                        if (ObjectInputStream.STREAM_DEBUG) {
                            log.info(in.hashCode() + ": Reading fixed element '" + ii + "'.");
                        }
                        Object element = _delegate.createObject(in);
                        in.readBareObject(element, _delegate, useReader);
                        Array.set(object, ii, element);
                    } else if (ObjectInputStream.STREAM_DEBUG) {
                        log.info(in.hashCode() + ": Skipping null element '" + ii + "'.");
                    }
                }

            } else {
                // otherwise we had to write each object out individually
                for (int ii = 0; ii < length; ii++) {
                    if (ObjectInputStream.STREAM_DEBUG) {
                        log.info(in.hashCode() + ": Reading free element '" + ii + "'.");
                    }
                    Array.set(object, ii, in.readObject());
                }
            }
            return;
        }

        // an enum has no additional state
        if (_target.isEnum()) {
            return;
        }

        // otherwise simply read the fields via our field marshallers
        int fcount = _fields.length;
        for (int ii = 0; ii < fcount; ii++) {
            Field field = _fields[ii];
            FieldMarshaller fm = _marshallers[ii];
            if (fm == null) {
                String errmsg = "Unable to marshall field [class=" + _target.getName() +
                    ", field=" + field.getName() + ", type=" + field.getType().getName() + "]";
                throw new IOException(errmsg);
            }
            try {
                if (ObjectInputStream.STREAM_DEBUG) {
                    log.info(in.hashCode() + ": Reading field '" + field.getName() + "' " +
                             "with " + fm + ".");
                }
                // gracefully deal with objects that have had new fields added to their class
                // definition
                if (in.available() > 0) {
                    fm.readField(field, object, in);
                } else {
                    log.info("Streamed instance missing field (probably newly added) " +
                             "[class=" + _target.getName() + ", field=" + field.getName() + "].");
                }
            } catch (Exception e) {
                String errmsg = "Failure reading streamable field [class=" + _target.getName() +
                    ", field=" + field.getName() + ", error=" + e + "]";
                throw (IOException) new IOException(errmsg).initCause(e);
            }
        }

        if (ObjectInputStream.STREAM_DEBUG) {
            log.info(in.hashCode() + ": Read object '" + object + "'.");
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return (_target == null) ? getClass().getName() :
            "[target=" + _target.getName() + ", delegate=" + _delegate +
            ", fcount=" + (_fields == null ? 0 : _fields.length) +
            ", reader=" + _reader + ", writer=" + _writer + "]";
    }

    /**
     * The constructor used by the basic streamers.
     */
    protected Streamer ()
    {
    }

    /**
     * Constructs a streamer for the specified target class.
     */
    protected Streamer (Class<?> target)
        throws IOException
    {
        // keep a handle on the class
        _target = target;

        // if our target class is an array, we want to get a handle on a streamer delegate that
        // we'll use to stream our elements
        if (_target.isArray()) {
            _delegate = Streamer.getStreamer(_target.getComponentType());
            // sanity check
            if (_delegate == null) {
                String errmsg = "Aiya! Streamer created for array type but we have no registered " +
                    "streamer for the element type [type=" + _target.getName() + "]";
                throw new RuntimeException(errmsg);
            }
            // and that's all we'll need
            return;
        }

        // look up the reader and writer methods
        try {
            _reader = target.getMethod(READER_METHOD_NAME, READER_ARGS);
        } catch (NoSuchMethodException nsme) {
            // nothing to worry about, we just don't have one
        }
        try {
            _writer = target.getMethod(WRITER_METHOD_NAME, WRITER_ARGS);
        } catch (NoSuchMethodException nsme) {
            // nothing to worry about, we just don't have one
        }

        // reflect on all the object's fields
        _fields = ClassUtil.getFields(target);
        int fcount = _fields.length;

        // obtain field marshallers for all of our fields
        _marshallers = new FieldMarshaller[fcount];
        for (int ii = 0; ii < fcount; ii++) {
            _marshallers[ii] = FieldMarshaller.getFieldMarshaller(_fields[ii]);
            if (ObjectInputStream.STREAM_DEBUG) {
                log.info("Using " + _marshallers[ii] + " for " + _target.getName() + "." +
                         _fields[ii].getName() + ".");
            }
        }
    }

    /**
     * Creates our streamers table and registers streamers for all of the basic types.
     */
    protected static void createStreamers ()
    {
        _streamers = new HashMap<Class,Streamer>();

        // register all of the basic streamers
        int bscount = BasicStreamers.BSTREAMER_TYPES.length;
        for (int ii = 0; ii < bscount; ii++) {
            _streamers.put(BasicStreamers.BSTREAMER_TYPES[ii],
                           BasicStreamers.BSTREAMER_INSTANCES[ii]);
        }
    }

    /** The class for which this streamer instance is configured. */
    protected Class _target;

    /** If our target class is an array, this is a reference to a streamer that can stream our
     * array elements, otherwise it is null. */
    protected Streamer _delegate;

    /** The non-transient, non-static public fields that we will stream when requested. */
    protected Field[] _fields;

    /** Field marshallers for each field that will be read or written in our objects. */
    protected FieldMarshaller[] _marshallers;

    /** A reference to the <code>readObject</code> method if one is defined by our target class. */
    protected Method _reader;

    /** A reference to the <code>writeObject</code> method if one is defined by our target
     * class. */
    protected Method _writer;

    /** Contains the mapping from class names to configured streamer instances. */
    protected static HashMap<Class,Streamer> _streamers;

    /** The name of the custom reader method. */
    protected static final String READER_METHOD_NAME = "readObject";

    /** The argument list for the custom reader method. */
    protected static final Class[] READER_ARGS = { ObjectInputStream.class };

    /** The name of the custom writer method. */
    protected static final String WRITER_METHOD_NAME = "writeObject";

    /** The argument list for the custom writer method. */
    protected static final Class[] WRITER_ARGS = { ObjectOutputStream.class };
}
