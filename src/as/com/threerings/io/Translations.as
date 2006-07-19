package com.threerings.io {

import com.threerings.util.HashMap;

/**
 * Maintains a set of translations between actionscript class names
 * and server (Java) class names.
 */
public class Translations
{
    public static function getToServer (asName :String) :String
    {
        var javaName :String = (_toServer.get(asName) as String);
        return (javaName == null) ? asName.replace("_", "$") : javaName;
    }

    public static function getFromServer (javaName :String) :String
    {
        var asName :String = (_fromServer.get(javaName) as String);
        return (asName == null) ? javaName.replace("$", "_") : asName;
    }

    public static function addTranslation (
            asName :String, javaName :String) :void
    {
        _toServer.put(asName, javaName);
        _fromServer.put(javaName, asName);
    }

    /** A mapping of actionscript names to java names. */
    protected static var _toServer :HashMap = new HashMap();

    /** A mapping of java names to actionscript names. */
    protected static var _fromServer :HashMap = new HashMap();

    // initialize some standard classes
    addTranslation("Object", "java.lang.Object");
    addTranslation("com.threerings.util.langBoolean", "java.lang.Boolean");
}
}
