//
// $Id: ObjectOutputStream.java,v 1.2 2004/02/25 14:42:46 mdb Exp $

package com.threerings.io;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Used to write {@link Streamable} objects to an {@link OutputStream}.
 * Other common object types are supported as well:
 *
 * <pre>
 * Boolean
 * Byte
 * Character
 * Short
 * Integer
 * Long
 * Float
 * Double
 * boolean[]
 * byte[]
 * char[]
 * short[]
 * int[]
 * long[]
 * float[]
 * double[]
 * Object[]
 * </pre>
 *
 * @see Streamable
 */
public class ObjectOutputStream extends DataOutputStream
{
    /**
     * Constructs an object output stream which will write its data to the
     * supplied target stream.
     */
    public ObjectOutputStream (OutputStream target)
    {
        super(target);
    }

    /**
     * Writes a {@link Streamable} instance or one of the support object
     * types to the output stream.
     */
    public void writeObject (Object object)
        throws IOException
    {
        // if the object to be written is null, simply write a zero
        if (object == null) {
            writeShort(0);
            return;
        }

        // create our classmap if necessary
        if (_classmap == null) {
            _classmap = new HashMap();
        }

        try {
            // otherwise, look up the class mapping record
            Class sclass = object.getClass();
            ClassMapping cmap = (ClassMapping)_classmap.get(sclass);
            _current = object;

            // create a class mapping for this class if we've not got one
            if (cmap == null) {
                // create a streamer instance and assign a code to this class
                Streamer streamer = Streamer.getStreamer(sclass);
                // we specifically do not inline the getStreamer() call
                // into the ClassMapping constructor because we want to be
                // sure not to call _nextCode++ if getStreamer() throws an
                // exception
                cmap = new ClassMapping(_nextCode++, sclass, streamer);
                _classmap.put(sclass, cmap);

                // make sure we didn't blow past our maximum class count
                if (_nextCode <= 0) {
                    throw new RuntimeException("Too many unique classes " +
                                               "written to ObjectOutputStream");
                }

                // writing a negative class code indicates that the class
                // name will follow
                writeShort(-cmap.code);
                writeUTF(sclass.getName());

            } else {
                writeShort(cmap.code);
            }

            // now write the instance data
            _streamer = cmap.streamer;
            _streamer.writeObject(object, this, true);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    /**
     * Writes a {@link Streamable} instance or one of the support object
     * types <em>without associated class metadata</em> to the output
     * stream. The caller is responsible for knowing the exact class of
     * the written object, creating an instance of such and calling {@link
     * ObjectInputStream#readBareObject} to read its data from the stream.
     *
     * @param object the object to be written. It cannot be
     * <code>null</code>.
     */
    public void writeBareObject (Object object)
        throws IOException
    {
        try {
            // get the streamer for objects of this type
            _streamer = Streamer.getStreamer(object.getClass());
            _current = object;

            // now write the instance data
            _streamer.writeObject(object, this, true);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    /**
     * Uses the default streamable mechanism to write the contents of the
     * object currently being streamed. This can only be called from
     * within a <code>writeObject</code> implementation in a {@link
     * Streamable} object.
     */
    public void defaultWriteObject ()
        throws IOException
    {
        // sanity check
        if (_current == null) {
            throw new RuntimeException(
                "defaultWriteObject() called illegally.");
        }

//         Log.info("Writing default [cmap=" + _streamer +
//                  ", current=" + _current + "].");

        // write the instance data
        _streamer.writeObject(_current, this, false);
    }

    /**
     * Used by a {@link Streamer} that is writing an array of {@link
     * Streamable} instances.
     */
    protected void setCurrent (Streamer streamer, Object current)
    {
        _streamer = streamer;
        _current = current;
    }

    /** Used to map classes to numeric codes and the {@link Streamer}
     * instance used to write them. */
    protected HashMap _classmap;

    /** A counter used to assign codes to streamed classes. */
    protected short _nextCode = 1;

    /** The object currently being written to the stream. */
    protected Object _current;

    /** The streamer being used currently. */
    protected Streamer _streamer;
}
