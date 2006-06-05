package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Number objects.
 */
public class NumberStreamer extends Streamer
{
    public function NumberStreamer ()
    {
        super(Number, "java.lang.Double");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        return ins.readDouble();
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var n :Number = (obj as Number);
        out.writeDouble(n);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // nothing here, the Number is fully read in createObject()
    }
}
}
