package com.threerings.util {

/**
 * A Map is an object that maps keys to values.
 */
public interface Map
{
    /**
     * Clear this map, removing all stored elements.
     */
    function clear () :void;

    /**
     * Returns true if the specified key exists in the map.
     */
    function containsKey (key :Object) :Boolean;

    /**
     * Retrieve the value stored in this map for the specified key.
     * Returns the value, or undefined if there is no mapping for the key.
     */
    function get (key :Object) :*;

    /**
     * Call the specified function, which accepts two args: key and value,
     * for every mapping.
     */
    function forEach (fn :Function) :void;

    /**
     * Returns true if this map contains no elements.
     */
    function isEmpty () :Boolean;

    /**
     * Return all the unique keys in this Map, in Array form.
     * The Array is not a 'view': it can be modified without disturbing
     * the Map from whence it came.
     */
    function keys () :Array;

    /**
     * Store a value in the map associated with the specified key.
     * Returns the previous value stored for that key, or undefined.
     */
    function put (key :Object, value :Object) :*;

    /**
     * Removes the mapping for the specified key.
     * Returns the value that had been stored, or undefined.
     */
    function remove (key :Object) :*;

    /**
     * Return the current size of the map.
     */
    function size () :int;

    /**
     * Return all the values in this Map, in Array form.
     * The Array is not a 'view': it can be modified without disturbing
     * the Map from whence it came.
     */
    function values () :Array;
}

}
