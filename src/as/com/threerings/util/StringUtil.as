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
     * Does the specified string start with the specified substring.
     */
    public static function startsWith (str :String, substr :String) :Boolean
    {
        // just check once if it's at the beginning
        return (str.lastIndexOf(substr, 0) == 0);
    }

    /**
     * Append 0 or more copies of the padChar String to the input String
     * until it is at least the specified length.
     */
    public static function pad (
        str :String, length :int, padChar :String = " ") :String
    {
        while (str.length < length) {
            str += padChar;
        }
        return str;
    }

    /**
     * Prepend 0 or more copies of the padChar String to the input String
     * until it is at least the specified length.
     */
    public static function prepad (
        str :String, length :int, padChar :String = " ") :String
    {
        while (str.length < length) {
            str = padChar + str;
        }
        return str;
    }

    /**
     * Utility function that strips whitespace from the ends of a String.
     */
    public static function trim (str :String) :String
    {
        return mx.utils.StringUtil.trim(str);
    }

    /**
     * Truncate the specified String if it is longer than maxLength.
     * The string will be truncated at a position such that it is
     * maxLength chars long after the addition of the 'append' String.
     *
     * @param append a String to add to the truncated String only after
     * truncation.
     */
    public static function truncate (
        s :String, maxLength :int, append :String = "") :String
    {
        if ((s == null) || (s.length <= maxLength)) {
            return s;
        } else {
            return s.substring(0, maxLength - append.length) + append;
        }
    }
}
}
