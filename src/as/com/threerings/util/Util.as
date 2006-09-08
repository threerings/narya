package com.threerings.util {

import flash.utils.ByteArray;

import com.threerings.util.StringBuilder;

public class Util
{
    /**
     * A nice utility method for testing equality in a better way.
     * If the objects are Equalable, then that will be tested. Arrays
     * and ByteArrays are also compared and are equal if they have
     * elements that are equals (deeply).
     */
    public static function equals (obj1 :Object, obj2 :Object) :Boolean
    {
        // catch various common cases (both primitive or null)
        if (obj1 === obj2) {
            return true;

        } else if (obj1 is Equalable) {
            // if obj1 is Equalable, then that decides it
            return (obj1 as Equalable).equals(obj2);

        } else if ((obj1 is Array) && (obj2 is Array)) {
            var ar1 :Array = (obj1 as Array);
            var ar2 :Array = (obj2 as Array);
            if (ar1.length != ar2.length) {
                return false;
            }
            for (var jj :int = 0; jj < ar1.length; jj++) {
                if (!equals(ar1[jj], ar2[jj])) {
                    return false;
                }
            }
            return true;

        } else if ((obj1 is ByteArray) && (obj2 is ByteArray)) {
            var ba1 :ByteArray = (obj1 as ByteArray);
            var ba2 :ByteArray = (obj2 as ByteArray);
            if (ba1.length != ba2.length) {
                return false;
            }
            for (var ii :int = 0; ii < ba1.length; ii++) {
                if (ba1[ii] != ba2[ii]) {
                    return false;
                }
            }
            return true;
        }

        return false;
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
