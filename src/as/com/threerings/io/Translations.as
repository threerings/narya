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
    addTranslation("String", "java.lang.String");
    addTranslation("com.threerings.util.langBoolean", "java.lang.Boolean");
}
}
