package com.threerings.util {

public interface Map
{
    function clear () :void;

    function containsKey (key :Object) :Boolean;

    function get (key :Object) :*;

    function isEmpty () :Boolean;

    function put (key :Object, value :Object) :*;

    function remove (key :Object) :*;

    function size () :int;
}

}
