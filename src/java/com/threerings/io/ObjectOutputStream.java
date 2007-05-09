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

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.threerings.NaryaLog.log;

/**
 * Used to write {@link Streamable} objects to an {@link OutputStream}.  Other common object types
 * are supported as well: <code>Boolean, Byte, Character, Short, Integer, Long, Float, Double,
 * boolean[], byte[], char[], short[], int[], long[], float[], double[], Object[]</code>.
 *
 * @see Streamable
 */
public class ObjectOutputStream extends DataOutputStream
{
    /**
     * Constructs an object output stream which will write its data to the supplied target stream.
     */
    public ObjectOutputStream (OutputStream target)
    {
        super(target);
    }

    /**
     * Configures this object output stream with a mapping from a classname to a streamed name.
     */
    public void addTranslation (String className, String streamedName)
    {
        if (_translations == null) {
            _translations = new HashMap<String,String>();
        }
        _translations.put(className, streamedName);
    }

    /**
     * Writes a {@link Streamable} instance or one of the support object types to the output
     * stream.
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
            _classmap = new HashMap<Class,ClassMapping>();
        }

        // otherwise, look up the class mapping record
        Class sclass = Streamer.getStreamerClass(object);
        ClassMapping cmap = _classmap.get(sclass);

        // create a class mapping for this class if we've not got one
        if (cmap == null) {
            // create a streamer instance and assign a code to this class
            Streamer streamer = Streamer.getStreamer(sclass);
            // we specifically do not inline the getStreamer() call into the ClassMapping
            // constructor because we want to be sure not to call _nextCode++ if getStreamer()
            // throws an exception
            if (ObjectInputStream.STREAM_DEBUG) {
                log.info(hashCode() + ": Creating class mapping [code=" + _nextCode +
                         ", class=" + sclass.getName() + "].");
            }
            cmap = new ClassMapping(_nextCode++, sclass, streamer);
            _classmap.put(sclass, cmap);

            // make sure we didn't blow past our maximum class count
            if (_nextCode <= 0) {
                throw new RuntimeException("Too many unique classes written to ObjectOutputStream");
            }

            // writing a negative class code indicates that the class name will follow
            writeShort(-cmap.code);
            String cname = sclass.getName();
            if (_translations != null) {
                String tname = _translations.get(cname);
                if (tname != null) {
                    cname = tname;
                }
            }
            writeUTF(cname);

        } else {
            writeShort(cmap.code);
        }

        writeBareObject(object, cmap.streamer, true);
    }

    /**
     * Writes a {@link Streamable} instance or one of the support object types <em>without
     * associated class metadata</em> to the output stream. The caller is responsible for knowing
     * the exact class of the written object, creating an instance of such and calling {@link
     * ObjectInputStream#readBareObject} to read its data from the stream.
     *
     * @param object the object to be written. It cannot be <code>null</code>.
     */
    public void writeBareObject (Object object)
        throws IOException
    {
        writeBareObject(object, Streamer.getStreamer(Streamer.getStreamerClass(object)), true);
    }

    /**
     * Write a {@link Streamable} instance without associated class metadata.
     */
    protected void writeBareObject (Object obj, Streamer streamer, boolean useWriter)
        throws IOException
    {
        _current = obj;
        _streamer = streamer;
        try {
            _streamer.writeObject(obj, this, useWriter);
        } finally {
            _current = null;
            _streamer = null;
        }
    }

    /**
     * Uses the default streamable mechanism to write the contents of the object currently being
     * streamed. This can only be called from within a <code>writeObject</code> implementation in a
     * {@link Streamable} object.
     */
    public void defaultWriteObject ()
        throws IOException
    {
        // sanity check
        if (_current == null) {
            throw new RuntimeException("defaultWriteObject() called illegally.");
        }

//         log.info("Writing default [cmap=" + _streamer + ", current=" + _current + "].");

        // write the instance data
        _streamer.writeObject(_current, this, false);
    }

    /** Used to map classes to numeric codes and the {@link Streamer} instance used to write
     * them. */
    protected HashMap<Class,ClassMapping> _classmap;

    /** A counter used to assign codes to streamed classes. */
    protected short _nextCode = 1;

    /** The object currently being written to the stream. */
    protected Object _current;

    /** The streamer being used currently. */
    protected Streamer _streamer;

    /** An optional set of class name translations to use when serializing objects. */
    protected HashMap<String,String> _translations;
}
