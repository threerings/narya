package com.threerings.util {

public class StringUtil
{
    public static function isBlank (str :String) :Boolean
    {
        return (str == null) || (str.search("\\S") == -1);
    }
}
}
