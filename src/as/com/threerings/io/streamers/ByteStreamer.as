package com.threerings.io.streamers {

import com.threerings.util.Byte;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Byte objects.
 */
public class ByteStreamer extends Streamer
{
    public function ByteStreamer ()
    {
        super(Byte, "java.lang.Byte");
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return new Byte(ins.readByte());
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var byte :Byte = (obj as Byte);
        out.writeByte(byte.value);
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // unneeded, done in createObject
    }
}
}
