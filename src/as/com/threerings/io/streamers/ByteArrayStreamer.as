package com.threerings.io.streamers {

import flash.utils.ByteArray;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for ByteArray objects.
 */
public class ByteArrayStreamer extends Streamer
{
    public function ByteArrayStreamer ()
    {
        super(ByteArray, "[B"); // yes, that's the Java class for a byte[].
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        var bytes :ByteArray = new ByteArray();
        bytes.length = ins.readInt();
        return bytes;
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        ins.readBytes(bytes, 0, bytes.length);
    }
}
}
