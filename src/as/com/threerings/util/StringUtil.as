package com.threerings.util {

import mx.utils.*;

public class StringUtil
{
    /**
     * Get a reasonable hash code for the specified String.
     */
    public static function hashCode (str :String) :int
    {
        var code :int = 0;
        if (str != null) {
            // sample at most 8 chars
            var lastChar :int = Math.min(8, str.length);
            for (var ii :int = 0; ii < lastChar; ii++) {
                code = code * 31 + str.charCodeAt(ii);
            }
        }
        return code;
    }

    public static function isBlank (str :String) :Boolean
    {
        return (str == null) || (str.search("\\S") == -1);
    }

    /**
     * Does the specified string end with the specified substring.
     */
    public static function endsWith (str :String, substr :String) :Boolean
    {
        var startDex :int = str.length - substr.length;
        return (startDex >= 0) && (str.indexOf(substr, startDex) >= 0);
    }

    /**
     * Utility function that strips whitespace from the ends of a String.
     */
    public static function trim (str :String) :String
    {
        return mx.utils.StringUtil.trim(str);
    }
}
}
