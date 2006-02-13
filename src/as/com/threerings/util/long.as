package com.threerings.util {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * TODO: stuff.
 */
public class long extends Object
    implements Streamable
{
    public function long (lowbits :int, highbits :int = 0)
    {
        _lowbits = lowbits;
        _highbits = highbits;
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        out.writeInt(_highbits);
        out.writeInt(_lowbits);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        _highbits = ins.readInt();
        _lowbits = ins.readInt();
    }

    /** Yon bits. */
    protected var _lowbits :int, _highbits :int;
}
}
