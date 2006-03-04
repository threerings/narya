package com.threerings.util {

import flash.util.describeType;

public class ClassUtil
{
    public static function getClassName (obj :Object) :String
    {
        return flash.util.getQualifiedClassName(obj).replace("::", ".");
    }

    public static function shortClassName (obj :Object) :String
    {
        var s :String = flash.util.getQualifiedClassName(obj);
        var dex :int = s.lastIndexOf(".");
        if (dex != -1) {
            s = s.substring(dex);
        }
        return s.replace("::", ".");
    }

    public static function getClass (obj :Object) :Class
    {
        return flash.util.getClassByName(getClassName(obj));
    }

    public static function getClassByName (cname :String) :Class
    {
        return flash.util.getClassByName(cname);
    }

    public static function isFinal (type :Class) :Boolean
    {
        var attrs :XMLList = describeType(type).elements("type");

        // FUCK!!! This information is lost in the Class introspection

        return true; //TODO
    }
}
}
