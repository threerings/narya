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

import flash.utils.ByteArray;

import com.threerings.util.ClassUtil;
import com.threerings.util.Cloneable;

public dynamic class TypedArray extends Array
    implements Cloneable
{
    /**
     * Convenience method to get the java type of an array containing objects of the specified
     * class.
     */
    public static function getJavaType (of :Class) :String
    {
        if (of === Boolean) {
            return "[Z";

        } else if (of === int) { // Number will be int if something like 3.0
            return "[I";

        } else if (of === Number) {
            return "[D";

        } else if (of === ByteArray) {
            return "[[B";
        }

        var cname :String = Translations.getToServer(ClassUtil.getClassName(of));
        return "[L" + cname + ";";
    }

    /**
     * A factory method to create a TypedArray for holding objects of the specified type.
     */
    public static function create (of :Class) :TypedArray
    {
        return new TypedArray(getJavaType(of));
    }

    /**
     * Create a TypedArray
     *
     * @param jtype The java classname of this array, for example "[I" to represent an int[], or
     * "[Ljava.lang.Object;" for Object[].
     */
    public function TypedArray (jtype :String)
    {
        _jtype = jtype;
    }

    /**
     * Adds all of the elements of the supplied array to this typed array. The types of the
     * elements of the target array must, of course, be of the type specified for this array
     * otherwise badness will ensue.
     */
    public function addAll (other :Array) :void
    {
        for (var ii :int = 0; ii < other.length; ii++) {
            push(other[ii]);
        }
    }

    public function getJavaType () :String
    {
        return _jtype;
    }

    // from Cloneable
    public function clone () :Object
    {
        var clazz :Class = ClassUtil.getClass(this);
        var copy :TypedArray = new clazz(_jtype);
        for (var ii :int = length - 1; ii >= 0; ii--) {
            copy[ii] = this[ii];
        }
        return copy;
    }

    /** The 'type' of this array, which doesn't really mean anything except gives it a clue as to
     * how to stream to our server. */
    protected var _jtype :String;
}
}
