package com.threerings.io {

import flash.errors.IOError;

import flash.util.ByteArray;
import flash.util.IDataInput;

import com.threerings.util.ClassUtil;
import com.threerings.util.SimpleMap;

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
    public function setSource (source :IDataInput)
    {
        _source = source;
    }

    public function readObject () :*
        //throws IOError
    {
        var cmap :ClassMapping;

        try {
            // read in the class code for this instance
            var code :int = readShort();

            // a zero code indicates a null value
            if (code == 0) {
                return null;

            // if the code is negative, that means we've never seen it
            // before and class metadata follows
            } else if (code < 0) {
                // first swap the code into positive land
                code *= -1;

                // read in the class metadata
                var cname :String = readUTF();
                var streamer :Streamer = Streamer.getStreamer(cname);

                cmap = new ClassMapping(code, cname, streamer);
                _classMap[code] = cmap;

            } else {
                cmap = _classMap[code];
                if (null == cmap) {
                    throw new IOError("Read object for which we have no " +
                        "registered class metadata.");
                }
            }

            // create an instance of the appropriate object
            _streamer = cmap.streamer;
            var target :* = _streamer.createObject(this);
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

    public function readBareObject (obj :*) :void
        //throws IOError
    {
        try {
            // obtain the streamer for the objects of this class
            _streamer = Streamer.getStreamer(ClassUtil.getClassName(obj));
            _current = obj;

            // now read the instance data
            _streamer.readObject(obj, this, true);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    public function readField (clazz :Class) :*
        //throws IOError
    {
        if (readBoolean()) {
            var obj :* = new clazz();
            return readBareObject(obj);
        }
        return null;
    }

    public function defaultReadObject () :void
        //throws IOError
    {
        _streamer.readObject(_current, this, false);
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

    /**
     * Used by a Streamer that is reading an array of Streamable instances.
     */
    protected function setCurrent (streamer :Streamer, current :*)
    {
        _streamer = streamer;
        _current = current;
    }

    /** The target DataInput that we route input from. */
    protected var _source :IDataInput;

    /** The object currently being read from the stream. */
    protected var _current :*;

    /** The stramer being used currently. */
    protected var _streamer :Streamer;

    /** A map of short class code to ClassMapping info. */
    protected var _classMap :SimpleMap = new SimpleMap();
}
}
