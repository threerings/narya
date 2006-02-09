package com.threerings.io {

public interface Streamable
{
    function writeObject (out :ObjectOutputStream) :void;
        //throws IOError;

    function readObject (ins :ObjectInputStream) :void;
        //throws IOError; /** ClassCastException equivalent. */
}
}
