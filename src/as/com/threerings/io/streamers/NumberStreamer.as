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
        super(Number);
    }

    public override function createObject (ins :ObjectInputStream) :*
    {
        return ins.readDouble();
    }

    public override function writeObject (obj :*, out :ObjectOutputStream,
            useWriter :Boolean) :void
    {
        var n :Number = (obj as Number);
        out.writeDouble(n);
    }

    public override function readObject (obj :*, ins :ObjectInputStream,
            useReader :Boolean) :void
    {
        // nothing here, the Number is fully read in createObject()
    }
}
}
