package com.threerings.util {

/**
 * Marker class that allows us to use ActionScript's built-in hashing
 * function without hassle.
 */
public dynamic class SimpleMap extends Object
{
    public function remove (key :Object) :Object
    {
        // I'm not sure this really works
        var skey :String = key.toString();
        var value :Object = this[skey];
        this[skey] = undefined;
        return value;
    }
}
}
