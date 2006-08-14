package com.threerings.util {

import flash.utils.describeType;
import flash.utils.getQualifiedClassName;
import flash.utils.getDefinitionByName;

public class ClassUtil
{
    public static function getClassName (obj :Object) :String
    {
        return getQualifiedClassName(obj).replace("::", ".");
    }

    public static function shortClassName (obj :Object) :String
    {
        var s :String = getQualifiedClassName(obj);
        var dex :int = s.lastIndexOf(".");
        if (dex != -1) {
            s = s.substring(dex);
        }
        return s.replace("::", ".");
    }

    /**
     * Return a new instance that is the same class as the specified
     * object. The class must have a zero-arg constructor.
     */
    public static function newInstance (obj :Object) :Object
    {
        var clazz :Class = getClass(obj);
        return new clazz();
    }

    public static function isSameClass (obj1 :Object, obj2 :Object) :Boolean
    {
        return (obj1.constructor == obj2.constructor);
    }

    public static function getClass (obj :Object) :Class
    {
        return Class(obj.constructor);
    }

    public static function getClassByName (cname :String) :Class
    {
        try {
            // see also ApplicationDomain.currentDomain.getClass(cname)
            return (getDefinitionByName(cname.replace("::", ".")) as Class);

        } catch (error :ReferenceError) {
            var log :Log = Log.getLog(ClassUtil);
            log.warning("Unknown class: " + cname);
            log.logStackTrace(error);
        }
        return null; // error case
    }

    public static function isFinal (type :Class) :Boolean
    {
        if (type === String) {
            return true;
        }

        // TODO: there's currently no way to determine final from the class
        // I thought examining the prototype might do it, but no dice.
        // Fuckers!
        return false;
    }

    /**
     * Returns true if an object of type srcClass is a subclass of or
     * implements the interface represented by the asClass paramter.
     *
     * <code>
     * if (ClassUtil.isAssignableAs(Streamable, someClass)) {
     *     var s :Streamable = (new someClass() as Streamable);
     * </code>
     */
    public static function isAssignableAs (
            asClass :Class, srcClass :Class) :Boolean
    {
        if (asClass == srcClass) {
            return true;

        // if not the same class and srcClass is Object, we're done
        } else if (srcClass == Object) {
            return false;
        }

        // ok, let's introspect on the class and see what we've got.
        var typeInfo :XMLList = describeType(srcClass).child("factory");

        // See which classes we extend.
        var exts :XMLList = typeInfo.child("extendsClass").attribute("type");
        var type :String;
        for each (type in exts) {
            if (asClass == getClassByName(type)) {
                return true;
            }
        }

        // See which interfaces we implement.
        var imps :XMLList = typeInfo.child("implementsInterface")
            .attribute("type");
        for each (type in imps) {
            if (asClass == getClassByName(type)) {
                return true;
            }
        }

        return false;
    }
}
}
