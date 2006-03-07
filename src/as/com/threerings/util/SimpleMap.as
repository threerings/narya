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
        var oldValue :Object = _data[skey];
        _data[skey] = value;
        return oldValue;
    }

    public function remove (key :Object) :Object
    {
        var skey :String = key.toString();
        var value :Object = _data[skey];
        delete _data[skey];
        return value;
    }

    private var _data :Object = new Object();
}
}
