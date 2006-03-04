package com.threerings.io {

/**
 * Note: all Streamable instances should have a constructor that copes
 * with no arguments.
 */
public interface Streamable
{
    function writeObject (out :ObjectOutputStream) :void;
        //throws IOError;

    function readObject (ins :ObjectInputStream) :void;
        //throws IOError; /** ClassCastException equivalent. */
}
}
