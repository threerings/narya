package com.threerings.util {

/**
 * Marker class that allows us to use ActionScript's built-in hashing
 * function without hassle.
 */
public class SimpleMap extends Object
{
    public function clear () :void
    {
        _data = new Object();
    }

    public function get (key :Object) :Object
    {
        var skey :String = key.toString();
        return _data[skey];
    }

    public function keys () :Array
    {
        var arr :Array = new Array();
        for (var skey :String in _data) {
            arr.push(skey);
        }
        return arr;
    }

    public function put (key :Object, value :Object) :Object
    {
        var skey :String = key.toString();
        var oldValue :* = _data[skey];
        _data[skey] = value;
        if (oldValue === undefined) {
            _size++;
        }
        return (oldValue as Object);
    }

    public function remove (key :Object) :Object
    {
        var skey :String = key.toString();
        var value :Object = _data[skey];
        delete _data[skey];
        _size--;
        return value;
    }

    public function size () :int
    {
        return _size;
    }

    protected var _data :Object = new Object();

    protected var _size :int = 0;
}
}
