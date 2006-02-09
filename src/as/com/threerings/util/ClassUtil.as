package com.threerings.util {

import flash.util.*;

public class ClassUtil extends Object
{
    public static function getClassName (obj :*) :String
    {
        return getQualifiedClassName(obj).replace("::", ".");
    }
}
}
