package com.threerings.util {

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
        out.writeInt(_lowbits);
        out.writeInt(_highbits);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        _lowbits = ins.readInt();
        _highbits = ins.readInt();
    }

    /** Yon bits. */
    protected var _lowbits :int, _highbits :int;
}
}
