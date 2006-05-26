package com.threerings.io.streamers {

import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;
import com.threerings.io.TypedArray;

/**
 * A Streamer for Array objects.
 */
public class ArrayStreamer extends Streamer
{
    public function ArrayStreamer (jname :String = "[Ljava.lang.Object;")
    {
        super(TypedArray, jname);
        trace("got new ArrayStreamer for " + jname);

        var secondChar :String = jname.charAt(1);
        if (secondChar === "[") {
            // if we're a multi-dimensional array then we need a delegate
            _delegate = Streamer.getStreamerByJavaName(jname.substring(1));
            _isFinal = true; // it just is

        } else if (secondChar === "L") {
            // form is "[L<class>;"
            var baseClass :String = jname.substring(2, jname.length - 1);
            baseClass = Translations.getFromServer(baseClass);
            _elementType = ClassUtil.getClassByName(baseClass);
            _isFinal = ClassUtil.isFinal(_elementType);

        } else if (secondChar === "I") {
            _elementType = int;

        } else {
            Log.getLog(this).warning("Other array types are " +
                "currently not handled yet [jname=" + jname + "].");
            throw new Error("Unimplemented bit");
        }
    }

    public override function isStreamerFor (obj :Object) :Boolean
    {
        if (_jname === "[Ljava.lang.Object;") {
            // we fall back to streaming any array as Object
            return (obj is Array);

        } else {
            return (obj is TypedArray) &&
                ((obj as TypedArray).getJavaType() === _jname);
        }
    }

    public override function isStreamerForClass (clazz :Class) :Boolean
    {
        if (_jname === "[Ljava.lang.Object;") {
            return (clazz != TypedArray) &&
                ClassUtil.isAssignableAs(Array, clazz);

        } else {
            return false; // TODO: we're kinda fucked for finding a streamer
            // by class for TypedArrays here. The caller should be passing
            // the java name.
        }
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        var ta :TypedArray = new TypedArray(_jname);
        ta.length = ins.readInt();
        trace("Read array length as: " + ta.length);
        return ta;
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var arr :Array = (obj as Array);
        out.writeInt(arr.length);
        if (_elementType == int) {
            for (var ii :int = 0; ii < arr.length; ii++) {
                out.writeInt(arr[ii] as int);
            }

        } else if (_isFinal) {
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
        if (_elementType == int) {
            for (var ii :int = 0; ii < arr.length; ii++) {
                arr[ii] = ins.readInt();
            }

        } else if (_isFinal) {
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
