package com.threerings.util {

import flash.utils.ByteArray;

import com.threerings.util.StringBuilder;

public class Util
{
    /**
     * A nicer test for equals that works if the objects implement Equalable
     */
    public static function equals (obj1 :Object, obj2 :Object) :Boolean
    {
        // catch various common cases (both primitive or null)
        return (obj1 === obj2) ||
            // otherwise, they're only possibly still equal if Equalable
            ((obj1 is Equalable) && (obj1 as Equalable).equals(obj2));
    }

    public static function cast (obj :Object, clazz :Class) :Object
    {
        if (obj == null || obj is clazz) {
            return obj;
        } else {
            throw new TypeError("value is not a " + clazz);
        }
    }
}
}
