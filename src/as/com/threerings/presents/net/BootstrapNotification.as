package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;

public class BootstrapNotification extends DownstreamMessage
{
    public function getData () :BootstrapData
    {
        return _data;
    }

    public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _data = ins.readField(BootstrapData);
    }

    /** The data associated with this notification. */
    protected var _data :BootstrapData;
}
}
