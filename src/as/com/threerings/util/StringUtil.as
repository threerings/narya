package com.threerings.util {

public class StringUtil
{
    public static function isBlank (str :String) :Boolean
    {
        return (str == null) || (str.search("\\S") == -1);
    }

    /**
     * Utility function that strips whitespace from the ends of a String.
     */
    public static function trim (str :String) :String
    {
        while (str.search(/\s/) == 0) {
            str = str.substring(1);
        }
        do {
            var endstr :String = str.substring(str.length - 1);
            if (endstr.search(/\s/) != -1) {
                str = str.substring(0, str.length - 1);
            } else {
                break;
            }
        } while (true);

        return str;
    }
}
}
