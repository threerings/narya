package com.threerings.util {

import mx.collections.ArrayCollection;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class StreamableArrayList extends ArrayCollection
    implements Streamable
{
    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(source.length);
        for (var ii :int = 0; ii < source.length; ii++) {
            out.writeObject(source[ii]);
        }
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        var ecount :int = ins.readInt();
        for (var ii :int = 0; ii < ecount; ii++) {
            source[ii] = ins.readObject();
        }
    }
}
}
