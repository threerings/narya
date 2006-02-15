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

    public override function createObject (ins :ObjectInputStream) :*
    {
        return ins.readDouble();
    }

    public override function writeObject (obj :*, out :ObjectOutputStream) :void
    {
        var n :Number = (obj as Number);
        out.writeDouble(n);
    }

    public override function readObject (obj :*, ins :ObjectInputStream) :void
    {
        // nothing here, the Number is fully read in createObject()
    }
}
}
