package com.threerings.io.streamers {

import flash.util.ByteArray;

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

    public override function createObject (ins :ObjectInputStream) :*
    {
        var bytes :ByteArray = new ByteArray();
        bytes.length = ins.readInt();
        return bytes;
    }

    public override function writeObject (obj :*, out :ObjectOutputStream) :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    public override function readObject (obj :*, ins :ObjectInputStream) :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        ins.readBytes(bytes, 0, bytes.length);
    }
}
}
