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

import com.threerings.util.Map;

import flash.utils.Dictionary;

/**
 * An implemention of Map that uses a Dictionary internally for storage. Any Object (and null)
 * may be used as a key with no loss in efficiency.
 */
public class ObjectMap
    implements Map
{
    /** @inheritDoc */
    public function get (key :Object) :*
    {
        return _dict[key];
    }

    /** @inheritDoc */
    public function put (key :Object, value :Object) :*
    {
        var oldVal :* = _dict[key];
        _dict[key] = value;
        if (oldVal === undefined) {
            _size++;
        }

        return oldVal;
    }

    /** @inheritDoc */
    public function remove (key :Object) :*
    {
        var oldVal :* = _dict[key];
        if (oldVal !== undefined) {
            delete _dict[key];
            _size--;
        }

        return oldVal;
    }

    /** @inheritDoc */
    public function clear () :void
    {
        _dict = new Dictionary();
        _size = 0;
    }

    /** @inheritDoc */
    public function containsKey (key :Object) :Boolean
    {
        return (get(key) !== undefined);
    }

    /** @inheritDoc */
    public function size () :int
    {
        return _size;
    }

    /** @inheritDoc */
    public function isEmpty () :Boolean
    {
        return (_size == 0);
    }

    /** @inheritDoc */
    public function keys () :Array
    {
        var keys :Array = [];
        forEach0(function (k :*, v :*) :void {
            keys.push(k);
        });
        return keys;
    }

    /** @inheritDoc */
    public function values () :Array
    {
        var vals :Array = [];
        forEach0(function (k :*, v :*) :void {
            vals.push(v);
        });
        return vals;
    }

    /** @inheritDoc */
    public function forEach (fn :Function) :void
    {
        forEach0(fn);
    }

    /**
     * Internal forEach.
     * @private
     */
    protected function forEach0 (fn :Function) :void
    {
        for (var key :Object in _dict) {
            fn(key, _dict[key]);
        }
    }

    protected var _dict :Dictionary = new Dictionary();
    protected var _size :int = 0;
}

}
