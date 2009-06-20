//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.Hashable;

/** 
 * A wrapped key object HashMap. This is really an internal class to HashMap, and when
 * Flash CS4 is fixed, it will go nestle back into HashMap.as's luxurious folds.
 */
public class HashMap_KeyWrapper
    implements Hashable
{
    public var key :Object;

    public function HashMap_KeyWrapper (
            key :Object, equalsFn :Function, hashFn :Function)
    {
        this.key = key;
        _equalsFn = equalsFn;
        var hashValue :* = hashFn(key);
        if (hashValue is String) {
            var uid :String = (hashValue as String);
            // examine at most 32 characters of the string
            var inc :int = int(Math.max(1, Math.ceil(uid.length / 32)));
            for (var ii :int = 0; ii < uid.length; ii += inc) {
                _hash = (_hash << 1) ^ int(uid.charCodeAt(ii));
            }

        } else {
            _hash = int(hashValue);
        }
    }

    // documentation inherited from interface Hashable
    public function equals (other :Object) :Boolean
    {
        return (other is HashMap_KeyWrapper) &&
            Boolean(_equalsFn(key, (other as HashMap_KeyWrapper).key));
    }

    // documentation inherited from interface Hashable
    public function hashCode () :int
    {
        return _hash;
    }

    protected var _key :Object;
    protected var _hash :int;
    protected var _equalsFn :Function;
}
}
