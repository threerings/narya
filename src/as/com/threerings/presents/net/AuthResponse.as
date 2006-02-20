package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthResponse extends DownstreamMessage
{
    public function getData () :AuthResponseData
    {
        return _data;
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _data = ins.readObject();
    }

    protected var _data :AuthResponseData;
}
}
