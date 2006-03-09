package com.threerings.io.streamers {

import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;
import com.threerings.io.TypedArray;

import com.threerings.presents.Log;

/**
 * A Streamer for Array objects.
 */
public class ArrayStreamer extends Streamer
{
    public function ArrayStreamer (jname :String)
    {
        Log.debug("Created an array streamer for type: {" + jname + "}");
        super(TypedArray, jname);

        if (jname.charAt(1) === "[") {
            // if we're a multi-dimensional array then we need a delegate
            _delegate = Streamer.getStreamerByJavaName(jname.substring(1));
            _isFinal = true; // it just is

        } else {
            if (jname.charAt(1) === "L") {
                // form is "[L<class>;"
                var baseClass :String = jname.substring(2, jname.length - 1);
                baseClass = Translations.getFromServer(baseClass);
                _elementType = ClassUtil.getClassByName(baseClass);
                _isFinal = ClassUtil.isFinal(_elementType);

            } else {
                Log.warning("Other array types are currently not handled yet " +
                    "[jname=" + jname + "].");
                throw new Error("Unimplemented bit");
            }
        }
    }

    public override function isStreamerFor (obj :Object) :Boolean
    {
        return (obj is TypedArray) &&
            ((obj as TypedArray).getJavaType() === _jname);
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        var ta :TypedArray = new TypedArray(_jname);
        ta.length = ins.readInt();
        return ta;
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var arr :Array = (obj as Array);
        out.writeInt(arr.length);
        if (_isFinal) {
            var mask :ArrayMask = new ArrayMask(arr.length);
            for (var ii :int = 0; ii < arr.length; ii++) {
                if (arr[ii] != null) {
                    mask.setBit(ii);
                }
            }
            mask.writeTo(out);
            // now write the populated elements
            for (var ii :int = 0; ii < arr.length; ii++) {
                var element :Object = arr[ii];
                if (element != null) {
                    out.writeBareObjectImpl(element, _delegate);
                }
            }

        } else {
            for (var ii :int = 0; ii < arr.length; ii++) {
                out.writeObject(arr[ii]);
            }
        }
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        var arr :Array = (obj as Array);
        if (_isFinal) {
            var mask :ArrayMask = new ArrayMask();
            mask.readFrom(ins);
            for (var ii :int = 0; ii < length; ii++) {
                if (mask.isSet(ii)) {
                    var target :Object;
                    if (_delegate == null) {
                        target = new _elementType();
                    } else {
                        target = _delegate.createObject(ins);
                    }
                    ins.readBareObjectImpl(target, _delegate);
                    this[ii] = target;
                }
            } 

        } else {
            for (var ii :int = 0; ii < arr.length; ii++) {
                arr[ii] = ins.readObject();
            } 
        }
    }

    /** A streamer for our elements. */
    protected var _delegate :Streamer;

    /** If this is the final dimension of the array, the element type. */
    protected var _elementType :Class;

    /** Whether we're final or not: true if we're not the final dimension
     * or if the element type is final. */
    protected var _isFinal :Boolean;
}
}
