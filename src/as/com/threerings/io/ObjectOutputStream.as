package com.threerings.io {

import flash.utils.ByteArray;
import flash.utils.IDataOutput;

import com.threerings.util.ClassUtil;
import com.threerings.util.HashMap;

public class ObjectOutputStream
{
    public function ObjectOutputStream (targ :IDataOutput)
    {
        _targ = targ;
    }

    public function writeObject (obj :Object) :void
        //throws IOError
    {
        // if the object to be written is null (or undefined) write a zero
        if (obj == null) {
            writeShort(0);
            return;
        }

        var cname :String;
        if (obj is TypedArray) {
            cname = (obj as TypedArray).getJavaType();

        } else {
            cname = ClassUtil.getClassName(obj);
        }
        // look up the class mapping record
        var cmap :ClassMapping = (_classMap.get(cname) as ClassMapping);

        // create a class mapping if we've not got one
        if (cmap == null) {
            var streamer :Streamer = Streamer.getStreamer(obj);
            // streamer may be null to indicate a Streamable object
            if (streamer == Streamer.BAD_STREAMER) {
                // TODO
                Log.warning("OMG, cannot stream " + cname);
                return;
            }

            cmap = new ClassMapping(_nextCode++, cname, streamer);
            _classMap.put(cname, cmap);

            // TODO: if _nextCode blows short, log an error

            writeShort(-cmap.code);
            writeUTF((streamer == null) ? Translations.getToServer(cname)
                                        : streamer.getJavaClassName());

        } else {
            writeShort(cmap.code);
        }

        writeBareObjectImpl(obj, cmap.streamer);
    }

    public function writeBareObject (obj :Object) :void
        //throws IOError
    {
        writeBareObjectImpl(obj, Streamer.getStreamer(obj));
    }

    public function writeBareObjectImpl (obj :Object, streamer :Streamer) :void
    {
        // if it's Streamable, it goes straight through
        if (streamer == null) {
            obj.writeObject(this); // obj is a Streamable
            return;
        }

        // otherwise, stream it!
        _current = obj;
        _streamer = streamer;
        try {
            _streamer.writeObject(obj, this);
        } finally {
            _current = null;
            _streamer = null;
        }
    }

    // TODO: this is equivalent to marshalling a field for which there
    // is a basic streamer. Work needs doing here.
    public function writeField (val :Object) :void
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
        _streamer.writeObject(_current, this);
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
        _targ.writeShort(value);
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

    /** The target DataOutput that we route things to. */
    protected var _targ :IDataOutput;
    
    /** A counter used to assign codes  to streamed classes. */
    protected var _nextCode :int = 1;

    /** The object currently being written out. */
    protected var _current :Object;

    /** The streamer being used currently. */
    protected var _streamer :Streamer;

    /** A map of classname to ClassMapping info. */
    protected var _classMap :HashMap = new HashMap();
}
}
