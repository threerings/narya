package com.threerings.io.streamers {

import com.threerings.util.Integer;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Integer objects.
 */
public class IntegerStreamer extends Streamer
{
    public function IntegerStreamer ()
    {
        super(Integer, "java.lang.Integer");
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return new Integer(ins.readInt());
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var inty :Integer = (obj as Integer);
        out.writeInt(inty.value);
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // unneeded, done in createObject
    }
}
}
