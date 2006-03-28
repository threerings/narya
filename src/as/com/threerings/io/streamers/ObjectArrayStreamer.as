package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;
import com.threerings.io.TypedArray;

/**
 * A Streamer for Array objects.
 */
public class ObjectArrayStreamer extends Streamer
{
    public function ObjectArrayStreamer ()
    {
        super(Array, "[Ljava.lang.Object;");
    }

    public override function isStreamerFor (obj :Object) :Boolean
    {
        // don't let TypedArrays be streamed with this streamer
        return !(obj is TypedArray) && super.isStreamerFor(obj);
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return new Array(ins.readInt());
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var arr :Array = (obj as Array);
        com.threerings.presents.Log.debug("array length: " + arr.length);
        out.writeInt(arr.length);
        for (var ii :int = 0; ii < arr.length; ii++) {
            out.writeObject(arr[ii]);
        }
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        var arr :Array = (obj as Array);
        for (var ii :int = 0; ii < arr.length; ii++) {
            arr[ii] = ins.readObject();
        }
    }
}
}
