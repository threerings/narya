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

package com.threerings.io {

import flash.errors.IOError;
import flash.errors.MemoryError;

import flash.utils.ByteArray;
import flash.utils.IDataInput;

import com.threerings.util.ClassUtil;
import com.threerings.util.Log;

public class ObjectInputStream
{
    /** Enables verbose object I/O debugging. */
    public static const DEBUG :Boolean = false;

    public static const log :Log = Log.getLog(ObjectInputStream);

    public function ObjectInputStream (source :IDataInput = null)
    {
        if (source == null) {
            source = new ByteArray();
        }
        _source = source;
    }

    /**
     * Set a new source from which to read our data.
     */
    public function setSource (source :IDataInput) :void
    {
        _source = source;
    }

    public function readObject () :Object
        //throws IOError
    {
        var DEBUG_ID :String = "[" + (++_debugObjectCounter) + "] ";
        try {
            // read in the class code for this instance
            var code :int = readShort();

            // a zero code indicates a null value
            if (code == 0) {
                if (DEBUG) log.debug(DEBUG_ID + "Read null");
                return null;
            }

            var cmap :ClassMapping;

            // if the code is negative, that means we've never seen it
            // before and class metadata follows
            if (code < 0) {
                // first swap the code into positive land
                code *= -1;

                // read in the class metadata
                var jname :String = readUTF();
//                log.debug("read jname: " + jname);
                var streamer :Streamer = Streamer.getStreamerByJavaName(jname);
                if (streamer == Streamer.BAD_STREAMER) {
                    log.warning("OMG, cannot stream " + jname);
                    return null;
                }
//                log.debug("Got streamer (" + streamer + ")");

                var cname :String = Translations.getFromServer(jname);
                cmap = new ClassMapping(code, cname, streamer);
                _classMap[code] = cmap;
                if (DEBUG) log.debug(DEBUG_ID + "Created mapping: (" + code + "): " + cname);

            } else {
                cmap = (_classMap[code] as ClassMapping);
                if (null == cmap) {
                    throw new IOError("Read object for which we have no " +
                        "registered class metadata [code=" + code + "].");
                }
                if (DEBUG) {
                    log.debug(DEBUG_ID + "Read known code: (" + code + ": " + cmap.classname + ")");
                }
            }

//            log.debug("Creating object sleeve...");
            var target :Object;
            if (cmap.streamer === null) {
                var clazz :Class = ClassUtil.getClassByName(cmap.classname);
                target = new clazz();

            } else {
                target = cmap.streamer.createObject(this);
            }
            //log.debug("Reading object...");
            readBareObjectImpl(target, cmap.streamer);
            if (DEBUG) log.debug(DEBUG_ID + "Read object: " + target);
            return target;

        } catch (me :MemoryError) {
            throw new IOError("out of memory" + me.message);
        }
        return null; // not reached: compiler dumb
    }

    public function readBareObject (obj :Object) :void
        //throws IOError
    {
        readBareObjectImpl(obj, Streamer.getStreamer(obj));
    }

    public function readBareObjectImpl (obj :Object, streamer :Streamer) :void
    {
        // streamable objects
        if (streamer == null) {
            (obj as Streamable).readObject(this);
            return;
        }

        _current = obj;
        _streamer = streamer;
        try {
            _streamer.readObject(obj, this);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    /**
     * @param type either a String representing the java type,
     *             or a Class object representing the actionscript type.
     */
    // TODO: this is the equivalent of marshalling something for which
    // we have a basic streamer. Fill out with all the java types
    public function readField (type :Object) :Object
        //throws IOError
    {
        if (readBoolean()) {
            var streamer :Streamer = (type is Class)
                ? Streamer.getStreamerByClass(type as Class)
                : Streamer.getStreamerByJavaName(type as String);

            if (streamer == Streamer.BAD_STREAMER) {
                throw new Error("Cannot field stream " + type);
            }

            var obj :Object;
            if (streamer != null) {
                obj = streamer.createObject(this);

            } else { 
                // create the streamable object, either by class or name
                var c :Class;
                if (type is Class) {
                    c = (type as Class);
                } else {
                    c = ClassUtil.getClassByName(
                        Translations.getFromServer(type as String));
                }
                obj = new c();
            }

            readBareObjectImpl(obj, streamer);
            return obj;
        }
        return null;
    }

    public function defaultReadObject () :void
        //throws IOError
    {
        _streamer.readObject(_current, this);
    }

    public function readBoolean () :Boolean
        //throws IOError
    {
        return _source.readBoolean();
    }

    public function readByte () :int
        //throws IOError
    {
        return _source.readByte();
    }

    public function readBytes (bytes :ByteArray, offset :uint = 0,
            length :uint = 0) :void
        //throws IOError
    {
        // IDataInput reads all available bytes if a length is not passed
        // in. Protect against an easy error to make by using the length of
        // the array
        if (length === 0) {
            length = bytes.length;
        }
        _source.readBytes(bytes, offset, length);
    }

    public function readDouble () :Number
        //throws IOError
    {
        return _source.readDouble();
    }

    public function readFloat () :Number
        //throws IOError
    {
        return _source.readFloat();
    }

    public function readInt () :int
        //throws IOError
    {
        return _source.readInt();
    }

    public function readShort () :int
        //throws IOError
    {
        return _source.readShort();
    }

    public function readUTF () :String
        //throws IOError
    {
        return _source.readUTF();
    }

    /** The target DataInput that we route input from. */
    protected var _source :IDataInput;

    /** The object currently being read from the stream. */
    protected var _current :Object;

    /** The stramer being used currently. */
    protected var _streamer :Streamer;

    /** A map of short class code to ClassMapping info. */
    protected var _classMap :Array = new Array();

    private static var _debugObjectCounter :int = 0;
}
}
