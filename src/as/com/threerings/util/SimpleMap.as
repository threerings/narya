package com.threerings.util {

import flash.util.Dictionary;

/**
 * I will likely extend this out to be a fully-featured map.
 */
public class SimpleMap extends Object
{
    public function clear () :void
    {
        _data = new Dictionary();
        _size = 0;
    }

    public function get (key :Object) :Object
    {
        return _data[key];
    }

    public function keys () :Array
    {
        var arr :Array = new Array();
        for (var key :Object in _data) {
            arr.push(key);
        }
        return arr;
    }

    public function put (key :Object, value :Object) :Object
    {
        var oldValue :* = _data[key];
        _data[key] = value;
        if (oldValue === undefined) {
            _size++;
        }
        return (oldValue as Object);
    }

    public function remove (key :Object) :Object
    {
        var value :* = _data[key];
        if (value !== undefined) {
            delete _data[key];
            _size--;
        }
        return (value as Object);
    }

    public function size () :int
    {
        return _size;
    }

    protected var _data :Dictionary = new Dictionary();

    protected var _size :int = 0;
}
}
