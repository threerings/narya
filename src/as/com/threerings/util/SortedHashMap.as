//
// $Id: HashMap.as 4848 2007-10-18 23:55:26Z ray $
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

package com.threerings.util {

/**
 * Guarantees a specific iteration order for keys(), values(), and forEach() with as little
 * additional overhead as possible.
 */
public class SortedHashMap extends HashMap
{
    public static const COMPARABLE_KEYS :int = 0;
    public static const STRING_KEYS :int = 1;
    public static const NUMERIC_KEYS :int = 2;

    public function SortedHashMap (
        keyType :int,
        loadFactor :Number = 1.75, equalsFn :Function = null, hashFn :Function = null)
    {
        super(loadFactor, equalsFn, hashFn);
        _keyType = keyType;
    }

    override public function put (key :Object, value :Object) :*
    {
        validateKey(key);
        return super.put(key, value);
    }

    override public function keys () :Array
    {
        var keys :Array = super.keys();

        switch (_keyType) {
        case COMPARABLE_KEYS:
            ArrayUtil.sort(keys);
            break;

        case STRING_KEYS:
        default:
            keys.sort();
            break;

        case NUMERIC_KEYS:
            keys.sort(Array.NUMERIC);
            break;
        }

        return keys;
    }

    override public function values () :Array
    {
        var keys :Array = keys();
        var vals :Array = new Array();
        for each (var key :Object in keys) {
            vals.push(get(key));
        }
        return vals;
    }

    override public function forEach (fn :Function) :void
    {
        var keys :Array = keys();
        for each (var key :Object in keys) {
            fn(key, get(key));
        }
    }

    protected function validateKey (key :Object) :void
        // throws ArgumentError
    {
        if (key == null) {
            return;
        }
        switch (_keyType) {
        case COMPARABLE_KEYS:
            if (key is Comparable) {
                return;
            }
            break;

        case STRING_KEYS:
            if (key is String) {
                return;
            }
            break;

        case NUMERIC_KEYS:
            if (key is Number) {
                return;
            }
            break;

        default:
            // there is no return
            break;
        }

        throw new ArgumentError("Invalid key");
    }

    /** The key type in use for this map. */
    protected var _keyType :int;
}
}
