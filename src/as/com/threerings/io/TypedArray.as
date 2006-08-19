package com.threerings.io {

import flash.utils.ByteArray;

import com.threerings.util.ClassUtil;
import com.threerings.util.Cloneable;

public dynamic class TypedArray extends Array
    implements Cloneable
{
    /**
     * Create a TypedArray
     *
     * @param jtype The java classname of this array, for example "[I" to
     * represent an int[], or "[Ljava.lang.Object;" for Object[].
     */
    public function TypedArray (jtype :String)
    {
        _jtype = jtype;
    }

    /**
     * Convenience method to get the java type of an array containing
     * objects of the specified class.
     */
    public static function getJavaType (of :Class) :String
    {
        if (of === Boolean) {
            return "[Z";

        } else if (of === int) { // Number will be int if something like 3.0
            return "[I";

        } else if (of === Number) {
            return "[D";

        } else if (of === ByteArray) {
            return "[[B";
        }

        var cname :String = Translations.getToServer(
            ClassUtil.getClassName(of));
        return "[L" + cname + ";";
    }

    /**
     * A factory method to create a TypedArray for holding objects
     * of the specified type.
     */
    public static function create (of :Class) :TypedArray
    {
        return new TypedArray(getJavaType(of));
    }

    public function getJavaType () :String
    {
        return _jtype;
    }

    // from Cloneable
    public function clone () :Object
    {
        var clazz :Class = ClassUtil.getClass(this);
        var copy :TypedArray = new clazz(_jtype);
        for (var ii :int = length - 1; ii >= 0; ii--) {
            copy[ii] = this[ii];
        }
        return copy;
    }

    /** The 'type' of this array, which doesn't really mean anything
     * except gives it a clue as to how to stream to our server. */
    protected var _jtype :String;
}
}
