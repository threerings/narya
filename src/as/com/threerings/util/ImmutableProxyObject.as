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

import flash.utils.Proxy;
import flash.utils.flash_proxy;

use namespace flash_proxy;

/**
 * Acts like the passed-in Object, but prevents modifications.
 */
public class ImmutableProxyObject extends Proxy
{
    public function ImmutableProxyObject (source :Object, throwErrors :Boolean = true)
    {
        _source = source;
        _throwErrors = throwErrors;
    }

//    public function hasOwnProperty (name :String) :Boolean
//    {
//        return _source.hasOwnProperty(name);
//    }
//
//    public function isPrototypeOf (theClass :Object) :Boolean
//    {
//        return _source.isPrototypeOf(theClass);
//    }
//
//    public function propertyIsEnumerable (name :String) :Boolean
//    {
//        return _source.propertyIsEnumerable(name);
//    }
//
//    public function setPropertyIsEnumerable (name :String, isEnum :Boolean = true) :void
//    {
//        immutable();
//    }

    public function toString () :String
    {
        return _source.toString();
    }

    // valueOf ?

    override flash_proxy function callProperty (name :*, ... rest) :*
    {
        Function(_source[name]).apply(null, rest);
    }

    override flash_proxy function deleteProperty (name :*) :Boolean
    {
        immutable();
        return false;
    }

    // omitted: getDescendants

    override flash_proxy function getProperty (key :*) :*
    {
        return _source[key];
    }

    override flash_proxy function hasProperty (key :*) :Boolean
    {
        return (key in _source);
    }

    // omitted: isAttribute

    override flash_proxy function nextName (index :int) :String
    {
        return _itrKeys[index - 1];
    }

    override flash_proxy function nextNameIndex (index :int) :int
    {
        if (index == 0) {
            _itrKeys = Util.keys(_source);
        }

        if (index < _itrKeys.length) {
            return index + 1;
        } else {
            _itrKeys = null;
            return 0;
        }
    }

    override flash_proxy function nextValue (index :int) :*
    {
        return _source[_itrKeys[index - 1]];
    }

    override flash_proxy function setProperty (name :*, value :*) :void
    {
        immutable();
    }

    protected function immutable () :void
    {
        if (_throwErrors) {
            throw new Error("You may not modify this object.");
        }
    }

    protected var _source :Object;

    protected var _throwErrors :Boolean;

    protected var _itrKeys :Array;
}
}
