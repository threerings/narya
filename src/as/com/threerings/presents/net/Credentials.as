package com.threerings.presents.net {

import com.threerings.io.*;
import com.threerings.util.Name;

public class Credentials
    implements Streamable
{
    public function Credentials (username :Name)
    {
        _username = username;
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        throws IOError
    {
    }

    // documentation inherited from interface Streamable
    public function readObject (in :ObjectInputStream) :void
        throws IOError
    {
    }

    /** The username. */
    protected var _username :Name;
}
}
