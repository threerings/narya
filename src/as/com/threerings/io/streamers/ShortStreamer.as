package com.threerings.io.streamers {

import com.threerings.util.Short;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Short objects.
 */
public class ShortStreamer extends Streamer
{
    public function ShortStreamer ()
    {
        super(Short, "java.lang.Short");
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return new Short(ins.readShort());
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var short :Short = (obj as Short);
        out.writeShort(short.value);
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // unneeded, done in createObject
    }
}
}
