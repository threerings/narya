//
// $Id$

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
