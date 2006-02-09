package com.threerings.io {

import flash.util.ByteArray;
import flash.util.IDataOutput;

import com.threerings.util.ClassUtil;
import com.threerings.util.SimpleMap;

public class ObjectOutputStream
//    implements IDataOutput
{
    public function ObjectOutputStream (targ :IDataOutput)
    {
        _targ = targ;
    }

    public function writeObject (obj :*) :void
        //throws IOError
    {
        // if the object to be written is null (or undefined) write a zero
        if (obj == null) {
            writeShort(0);
            return;
        }

        try {
            var cname :String = ClassUtil.getClassName(obj);

            // look up the class mapping record
            var cmap :ClassMapping = _classMap[cname];
            _current = obj;

            // create a class mapping if we've not got one
            if (cmap === undefined) {
                var streamer :Streamer = Streamer.getStreamer(cname);
                cmap = new ClassMapping(_nextCode++, cname, streamer);
                _classMap[cname] = cmap;

                // TODO: if _nextCode blows short, log an error

                writeShort(-cmap.code);
                writeUTF(cname);

            } else {
                writeShort(cmap.code);
            }

            // now write the instance data
            _streamer = cmap.streamer;
            _streamer.writeObject(obj, this, true);

        } finally {
            _current = null;
            _streamer = null;
        }
    }

    public function writeBareObject (obj :*) :void
        //throws IOError
    {
        try {
            // get the streamer for objects of this type
            _streamer = Streamer.getStreamer(ClassUtil.getClassName(obj));
            _current = obj;

            // now write the instance data
            _streamer.writeObject(obj, this, true);

        } finally {
            // clear out our current object references
            _current = null;
            _streamer = null;
        }
    }

    public function writeField (val :*) :void
        //throws IOError
    {
        var b :Boolean = (val != null);
        writeBoolean(b);
        if (b) {
            writeBareObject(val);
        }
    }

    /**
     * Uses the default streamable mechanism to write the contents of the
     * object currently being streamed. This can only be called from
     * within a <code>writeObject</code> implementation in a {@link
     * Streamable} object.
     */
    public function defaultWriteObject () :void
        //throws IOError
    {
        // sanity check
        if (_current == null) {
            throw new Error("defaultWriteObject() called illegally.");
        }

        // write the instance data
        _streamer.writeObject(_current, this, false)
    }

    public function writeBoolean (value :Boolean) :void
        //throws IOError
    {
        _targ.writeBoolean(value);
    }

    public function writeByte (value :int) :void
        //throws IOError
    {
        _targ.writeByte(value);
    }

    public function writeBytes (bytes :ByteArray, offset :uint=0,
            length :uint = 0) :void
        //throws IOError
    {
        _targ.writeBytes(bytes, offset, length);
    }

    public function writeDouble (value :Number) :void
        //throws IOError
    {
        _targ.writeDouble(value);
    }

    public function writeFloat (value :Number) :void
        //throws IOError
    {
        _targ.writeFloat(value);
    }

    public function writeInt (value :int) :void
        //throws IOError
    {
        _targ.writeInt(value);
    }

    public function writeShort (value :int) :void
        //throws IOError
    {
        _targ.writeInt(value);
    }

    public function writeUTF (value :String) :void
        //throws IOError
    {
        _targ.writeUTF(value);
    }

    // these two are defined in IDataOutput, but have no java equivalent so
    // we skip them.
    //public function writeUnsignedInt (value :int) :void
    //public function writeUTFBytes (value :int) :void

    /**
     * Used by a Streamer that is writing an array of Streamable instances.
     */
    protected function setCurrent (streamer :Streamer, current :*)
    {
        _streamer = streamer;
        _current = current;
    }

    /** The target DataOutput that we route things to. */
    protected var _targ :IDataOutput;
    
    /** A counter used to assign codes  to streamed classes. */
    protected var _nextCode :int = 1;

    /** The object currently being written out. */
    protected var _current :*;

    /** The streamer being used currently. */
    protected var _streamer :Streamer;

    /** A map of classname to ClassMapping inffaro. */
    protected var _classMap :SimpleMap = new SimpleMap();
}
}
