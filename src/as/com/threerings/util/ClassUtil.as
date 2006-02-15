package com.threerings.util {

public class ClassUtil
{
    public static function getClassName (obj :*) :String
    {
        return flash.util.getQualifiedClassName(obj).replace("::", ".");
    }

    public static function getClass (obj :*) :Class
    {
        return flash.util.getClassByName(getClassName(obj));
    }

    public static function getClassByName (cname :String) :Class
    {
        return flash.util.getClassByName(cname);
    }
}
}
