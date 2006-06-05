package com.threerings.io.streamers {

import com.threerings.util.Long;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Long objects.
 */
public class LongStreamer extends Streamer
{
    public function LongStreamer ()
    {
        super(Long, "java.lang.Long");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        return new Long(0);
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var longy :Long = (obj as Long);
        out.writeInt(longy.low);
        out.writeInt(longy.high);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        var longy :Long = (obj as Long);
        longy.low = ins.readInt();
        longy.high = ins.readInt();
    }
}
}
