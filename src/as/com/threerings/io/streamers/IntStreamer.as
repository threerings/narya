package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for int objects.
 */
public class IntStreamer extends Streamer
{
    public function IntStreamer ()
    {
        super(int, "java.lang.Integer");
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return ins.readInt();
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var i :int = (obj as int);
        out.writeInt(i);
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // nothing here, the int is fully read in createObject()
    }
}
}
