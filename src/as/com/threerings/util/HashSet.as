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

package com.threerings.util {

public class HashSet
   implements Set
{
    /**
     * Construct a HashSet
     *
     * @param loadFactor - A measure of how full the hashtable is allowed to
     *                     get before it is automatically resized. The default
     *                     value of 1.75 should be fine.
     * @param equalsFn   - (Optional) A function to use to compare object
     *                     equality for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (o1, o2) :Boolean".
     * @param hashFn     - (Optional) A function to use to generate a hash
     *                     code for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (obj) :*", where the return type is
     *                     numeric or String. Two objects that are equals
     *                     according to the specified equalsFn *must*
     *                     generate equal values when passed to the hashFn.
     */
    public function HashSet (
            loadFactor :Number = 1.75,
            equalsFn :Function = null,
            hashFn :Function = null)
    {
        _hashMap = new HashMap(loadFactor, equalsFn, hashFn);
    }

    public function add (o :Object) :Boolean
    {
        var previousValue :* = _hashMap.put(o, null);

        // return true if the key did not already exist in the Set
        return (undefined === previousValue);
    }

    public function remove (o :Object) :Boolean
    {
        var previousValue :* = _hashMap.remove(o);

        // return true if the key existed in the Set
        return (undefined !== previousValue);
    }

    public function clear () :void
    {
        _hashMap.clear();
    }

    public function contains (o :Object) :Boolean
    {
        return (_hashMap.containsKey(o));
    }

    public function size () :int
    {
        return (_hashMap.size());
    }

    public function isEmpty () :Boolean
    {
        return (_hashMap.isEmpty());
    }

    public function toArray () :Array
    {
        return (_hashMap.keys());
    }

    protected var _hashMap :HashMap;
}

}
