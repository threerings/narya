//
// $Id: ObjectInputStream.java,v 1.1 2002/07/23 05:42:34 mdb Exp $

package com.threerings.io;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.samskivert.util.HashIntMap;

import com.threerings.presents.Log;

/**
 * Used to read {@link Streamable} objects from an {@link InputStream}.
 * Other common object types are supported as well (@see {@link
 * ObjectOutputStream}).
 *
 * @see Streamable
 */
public class ObjectInputStream extends DataInputStream
{
    /**
     * Constructs an object input stream which will read its data from the
     * supplied source stream.
     */
    public ObjectInputStream (InputStream source)
    {
        super(source);
    }

    /**
     * Reads a {@link Streamable} instance or one of the supported object
     * types from the input stream.
     */
    public Object readObject ()
        throws IOException, ClassNotFoundException
    {
        ClassMapping cmap;

        // create our classmap if necessary
        if (_classmap == null) {
            _classmap = new HashIntMap();
        }

        try {
            // read in the class code for this instance
            short code = readShort();

            // a zero code indicates a null value
            if (code == 0) {
                return null;

            // if the code is negative, that means that we've never
            // seen it before and class metadata follows
            } else if (code < 0) {
                // first swap the code into positive-land
                code *= -1;

                // read in the class metadata, create a class mapping record
                // for the class, and cache it
                String cname = readUTF();
                Class sclass = Class.forName(cname);
                Streamer streamer = Streamer.getStreamer(sclass);

                // sanity check
                if (streamer == null) {
                    String errmsg = "Aiya! Unable to create streamer for " +
                        "newly seen class [code=" + code +
                        ", class=" + cname + "]";
                    throw new RuntimeException(errmsg);
                }

                cmap = new ClassMapping(code, sclass, streamer);
                _classmap.put(code, cmap);

            } else {
                cmap = (ClassMapping)_classmap.get(code);

                // sanity check
                if (cmap == null) {
                    String errmsg = "Aiya! Read object code for which we " +
                        "have no registered class metadata [code=" + code + "]";
                    throw new RuntimeException(errmsg);
                }
            }

            // create an instance of the appropriate object
            _streamer = cmap.streamer;
            Object target = _streamer.createObject(this);
            _current = target;

            // now read the instance data
            _streamer.readObject(target, this, true);

            // and return the newly read object
            return target;

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    /**
     * Reads an object from the input stream that was previously written
     * with {@link ObjectOutputStream#writeBareObject}.
     *
     * @param object the object to be populated from data on the stream.
     * It cannot be <code>null</code>.
     */
    public void readBareObject (Object object)
        throws IOException, ClassNotFoundException
    {
        try {
            // obtain the streamer for objects of this class
            _streamer = Streamer.getStreamer(object.getClass());
            _current = object;

            // now read the instance data
            _streamer.readObject(object, this, true);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    /**
     * Reads the fields of the specified {@link Streamable} instance from
     * the input stream using the default object streaming mechanisms (a
     * call is not made to <code>readObject()</code>, even if such a
     * method exists).
     */
    public void defaultReadObject ()
        throws IOException, ClassNotFoundException
    {
        // sanity check
        if (_current == null) {
            throw new RuntimeException(
                "defaultReadObject() called illegally.");
        }

        // read the instance data
        _streamer.readObject(_current, this, false);
    }

    /**
     * Used by a {@link Streamer} that is reading an array of {@link
     * Streamable} instances.
     */
    protected void setCurrent (Streamer streamer, Object current)
    {
        _streamer = streamer;
        _current = current;
    }

    /** Used to map classes to numeric codes and the {@link Streamer}
     * instance used to write them. */
    protected HashIntMap _classmap;

    /** The object currently being read from the stream. */
    protected Object _current;

    /** The streamer being used currently. */
    protected Streamer _streamer;
}
