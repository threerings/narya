package com.threerings.io {

import flash.utils.ByteArray;

public class ArrayMask
{
    public function ArrayMask (length :int = 0)
    {
        var mlength :int = int(length / 8);
        if (length % 8 != 0) {
            mlength++;
        }
        _mask.length = mlength;
    }

    /**
     * Set the specified index as containing a non-null element in the
     * array we're representing.
     */
    public function setBit (index :int) :void
    {
        _mask[int(index/8)] |= (1 << (index % 8));
    }

    /**
     * Is the specified array element non-null?
     */
    public function isSet (index :int) :Boolean
    {
        return (_mask[int(index/8)] & (1 << (index % 8))) != 0;
    }

    public function writeTo (out :ObjectOutputStream) :void
    {
        out.writeShort(_mask.length);
        out.writeBytes(_mask);
    }

    // documentation inherited from interface Streamable
    public function readFrom (ins :ObjectInputStream) :void
    {
        _mask.length = ins.readShort();
        ins.readBytes(_mask, 0, _mask.length);
    }

    /** The array mask. */
    protected var _mask :ByteArray = new ByteArray();
}
}
