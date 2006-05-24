package com.threerings.io {

import com.threerings.util.ClassUtil;

public dynamic class TypedArray extends Array
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
        var cname :String = Translations.getToServer(
            ClassUtil.getClassName(of));
        // TODO: primitive types
        return "[L" + cname + ";";
    }

    public function getJavaType () :String
    {
        return _jtype;
    }

    /** The 'type' of this array, which doesn't really mean anything
     * except gives it a clue as to how to stream to our server. */
    protected var _jtype :String;
}
}
