package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Array objects.
 */
public class ArrayStreamer extends Streamer
{
    public function ArrayStreamer ()
    {
        super(Array, "[Ljava.lang.Object");
    }

    public override function createObject (ins :ObjectInputStream) :*
    {
        return new Array(ins.readInt());
    }

    public override function writeObject (obj :*, out :ObjectOutputStream) :void
    {
        var arr :Array = (obj as Array);
        out.writeInt(arr.length);
        for (var ii :int = 0; ii < arr.length; ii++) {
            out.writeObject(arr[ii]);
        }
    }

    public override function readObject (obj :*, ins :ObjectInputStream) :void
    {
        var arr :Array = (obj as Array);
        for (var ii :int = 0; ii < arr.length; ii++) {
            arr[ii] = ins.readObject();
        }
    }
}
}
