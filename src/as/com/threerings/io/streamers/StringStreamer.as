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
        super(String, "java.lang.String");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        return ins.readUTF();
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var s :String = (obj as String);
        out.writeUTF(s);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // nothing here, the String is fully read in createObject()
    }
}
}
