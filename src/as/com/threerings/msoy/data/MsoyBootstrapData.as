package com.threerings.msoy.data {

import com.threerings.presents.net.BootstrapData;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class MsoyBootstrapData extends BootstrapData
{
    /** The oid of the chat room we should join. */
    public var chatOid :int;

    // documentation inherited
    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        chatOid = ins.readInt();
    }
}
}
