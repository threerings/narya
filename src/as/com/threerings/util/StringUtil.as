//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util {

import flash.utils.ByteArray;

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
     * Return true iff the first character is a lower-case character.
     */
    public static function isLowerCase (str :String) :Boolean
    {
        var firstChar :String = str.charAt(0);
        return (firstChar.toUpperCase() != firstChar) &&
            (firstChar.toLowerCase() == firstChar);
    }

    /**
     * Return true iff the first character is an upper-case character.
     */
    public static function isUpperCase (str :String) :Boolean
    {
        var firstChar :String = str.charAt(0);
        return (firstChar.toUpperCase() == firstChar) &&
            (firstChar.toLowerCase() != firstChar);
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
     * Substitute "{n}" tokens for the corresponding passed-in arguments.
     */
    public static function substitute (str :String, ... args) :String
    {
        // holy christ the varargs insanity
        args = Util.unfuckVarargs(args);
        args.unshift(str);
        return mx.utils.StringUtil.substitute.apply(null, args);
    }

    /**
     * Utility function that strips whitespace from the ends of a String.
     */
    public static function trim (str :String) :String
    {
        return mx.utils.StringUtil.trim(str);
    }

    public static function toString (obj :Object) :String
    {
        if (obj is Array) {
            var arr :Array = (obj as Array);
            var s :String = "Array(";
            for (var ii :int = 0; ii < arr.length; ii++) {
                if (ii > 0) {
                    s += ", ";
                }
                s += (ii + ": " + toString(arr[ii]));
            }
            return s + ")";
        }

        return String(obj);
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

    /**
     * Locate URLs in a string, return an array in which even elements
     * are plain text, odd elements are urls (as Strings). Any even element
     * may be an empty string.
     */
    public static function parseURLs (s :String) :Array
    {
        var array :Array = [];
        while (true) {
            var result :Object = URL_REGEXP.exec(s);
            if (result == null) {
                break;
            }

            var index :int = int(result.index);
            var url :String = String(result[0]);
            array.push(s.substring(0, index), url);
            s = s.substring(index + url.length);
        }

        // just the string is left
        array.push(s);
        return array;
    }

    /**
     * Generates a string from the supplied bytes that is the hex encoded
     * representation of those byts. Returns the empty String for a
     * <code>null</code> or empty byte array.
     */
    public static function hexlate (bytes :ByteArray) :String
    {
        var str :String = "";
        if (bytes != null) {
            for (var ii :int = 0; ii < bytes.length; ii++) {
                var b :int = bytes[ii];
                str += HEX[b >> 4] + HEX[b & 0xF];
            }
        }
        return str;
    }

    /**
     * Turn a hexlated String back into a ByteArray.
     */
    public static function unhexlate (hex :String) :ByteArray
    {
        if (hex == null || (hex.length % 2 != 0)) {
            return null;
        }

        hex = hex.toLowerCase();
        var data :ByteArray = new ByteArray();
        for (var ii :int = 0; ii < hex.length; ii += 2) {
            var value :int = HEX.indexOf(hex.charAt(ii)) << 4;
            value += HEX.indexOf(hex.charAt(ii + 1));

            // TODO: verify
            // values over 127 are wrapped around, restoring negative bytes
            data[ii / 2] = value;
        }

        return data;
    }

    /** Hexidecimal digits. */
    protected static const HEX :Array = [ "0", "1", "2", "3", "4",
        "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" ];

    /** A regular expression that finds URLs. */
    protected static const URL_REGEXP :RegExp =
        // recognize some standard protocols, plus 'command', which we use internally
        new RegExp("(http|https|ftp|command)://\\S+", "i");
}
}
