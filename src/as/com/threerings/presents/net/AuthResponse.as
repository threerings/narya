package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthResponse extends DownstreamMessage
{
    public function getData () :AuthResponseData
    {
        return _data;
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _data = (ins.readObject() as AuthResponseData);
    }

    protected var _data :AuthResponseData;
}
}
