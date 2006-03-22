package com.threerings.util {

import mx.utils.*;

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
        return mx.utils.StringUtil.trim(str);
    }
}
}
