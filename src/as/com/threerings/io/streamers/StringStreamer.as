package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for String objects.
 */
public class StringStreamer extends Streamer
{
    public function StringStreamer ()
    {
        super(String);
    }

    public override function createObject (ins :ObjectInputStream) :*
    {
        return ins.readUTF();
    }

    public override function writeObject (obj :*, out :ObjectOutputStream,
            useWriter :Boolean) :void
    {
        var s :String = (obj as String);
        out.writeUTF(s);
    }

    public override function readObject (obj :*, ins :ObjectInputStream,
            useReader :Boolean) :void
    {
        // nothing here, the String is fully read in createObject()
    }
}
}
