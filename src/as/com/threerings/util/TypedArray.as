package com.threerings.util {

import mx.utils.ObjectUtil;

import com.threerings.io.ArrayMask;
import com.threerings.io.Streamer;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.Log;

public dynamic class TypedArray extends Array
    implements Streamable
{
    // TODO: remove the isFinal parameter and determine it
    // be introspecting on the class
    public function TypedArray (type :Class, isFinal :Boolean, length :int = 0)
    {
        super(length);
        _type = type;
        _final = isFinal;
        _delegate = Streamer.getStreamerByClass(type);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        if (_final) {
            var mask = new ArrayMask();
            mask.readFrom(ins);
            for (var ii :int = 0; ii < length; ii++) {
                if (mask.isSet(ii)) {
                    var target :Object;
                    if (_delegate == null) {
                        target = new _type();
                    } else {
                        target = _delegate.createObject(ins);
                    }
                    ins.readBareObjectImpl(target, _delegate);
                    this[ii] = target;
                }
            }

        } else {
            for (var ii :int = 0; ii < length; ii++) {
                this[ii] = ins.readObject();
            }
        }
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(length);
        if (_final) {
            var mask :ArrayMask = new ArrayMask(length);
            for (var ii :int = 0; ii < length; ii++) {
                if (this[ii] != null) {
                    mask.set(ii);
                }
            }
            mask.writeTo(out);
            // now write the populated elements
            for (var ii :int = 0; ii < length; ii++) {
                var element :Object = this[ii];
                if (element != null) {
                    out.writeBareObjectImpl(element, _delegate);
                }
            }

        } else {
            for (var ii :int = 0; ii < length; ii++) {
                out.writeObject(this[ii]);
            }
        }
    }

    /** The 'type' of this array, which doesn't really mean anything
     * except gives it a clue as to how to stream to our server. */
    protected var _type :Class;

    /** Whether or not the type of the array represents a final class. */
    protected var _final :Boolean;

    protected var _delegate :Streamer;
}
}
