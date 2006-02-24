package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.Log;

public /* abstract */ class DownstreamMessage
    implements Streamable
{
    /** The message id of the upstream message with which this downstream
     * message is associated (or -1 if it is not associated with any
     * upstream message). */
    public var messageId :int = -1;

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        Log.warning("This is client code: Downstream messages shouldn't " +
            "be written");
        //out.writeShort(messageId);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        messageId = ins.readShort();
    }
}
}
