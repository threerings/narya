package com.threerings.util {

import flash.util.ByteArray;
import flash.util.StringBuilder;

public class Util
{
    public static function bytesToString (bytes :ByteArray) :String
    {
        var buf :StringBuilder = new StringBuilder();
        for (var ii :int = 0; ii < bytes.length; ii++) {
            var b :int = bytes[ii];
            buf.append(HEX[b >> 4], HEX[b & 0xF]);
        }
        return buf.toString();
    }

    public static function cast (obj :Object, clazz :Class) :Object
    {
        if (obj == null || obj is clazz) {
            return obj;
        } else {
            throw new Error("wah");
        }
    }

    private static const HEX :Array = new Array("0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
}
}
