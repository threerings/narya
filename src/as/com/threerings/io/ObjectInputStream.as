package com.threerings.io {

import flash.errors.IOError;

import flash.utils.ByteArray;
import flash.utils.IDataInput;

import com.threerings.util.ClassUtil;
import com.threerings.util.SimpleMap;

//import com.threerings.presents.Log;

public class ObjectInputStream
{
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
        try {
            // read in the class code for this instance
            var code :int = readShort();

            // a zero code indicates a null value
            if (code == 0) {
                return null;
            }

            var cmap :ClassMapping;

            // if the code is negative, that means we've never seen it
            // before and class metadata follows
            if (code < 0) {
                // first swap the code into positive land
                code *= -1;

                // read in the class metadata
                var cname :String = Translations.getFromServer(readUTF());
                Log.debug("read cname: " + cname);
                var streamer :Streamer = Streamer.getStreamerByJavaName(cname);
                if (streamer == Streamer.BAD_STREAMER) {
                    Log.warning("OMG, cannot stream " + cname);
                    return null;
                }
                Log.debug("Got streamer (" + streamer + ")");

                cmap = new ClassMapping(code, cname, streamer);
                _classMap[code] = cmap;
                Log.debug("Created mapping for (" + code + "): " + cname);

            } else {
                Log.debug("Read known code: " + code);
                cmap = (_classMap[code] as ClassMapping);
                if (null == cmap) {
                    throw new IOError("Read object for which we have no " +
                        "registered class metadata.");
                }
            }

            Log.debug("Creating object sleeve...");
            var target :Object;
            if (cmap.streamer === null) {
                var clazz :Class = ClassUtil.getClassByName(cmap.classname);
                target = new clazz();

            } else {
                target = cmap.streamer.createObject(this);
            }
            Log.debug("Reading object...");
            readBareObjectImpl(target, cmap.streamer);
            Log.debug("Read object: " + target);
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
}
}
