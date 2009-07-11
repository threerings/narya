//
// $Id$

package com.threerings.util {

/**
 * ByteEnum utility methods.
 */
public class ByteEnumUtil
{
    /**
     * Returns the enum value with the specified code in the supplied enum class.
     * Throws ArgumentError if the enum lacks a value that maps to the supplied code.
     */
    public static function fromByte (clazz :Class, code :int) :Enum
    {
        // we could do something fancier than this O(n) impl, in the future...
        for each (var e :Enum in Enum.values(clazz)) {
            if (ByteEnum(e).toByte() == code) {
                return e;
            }
        }
        throw new ArgumentError(clazz + " has no value with code " + code);
    }
}
}
