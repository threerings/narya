package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthResponse extends DownstreamMessage
{
    public function getData () :AuthResponseData
    {
        return _data;
    }

    public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _data = ins.readField(AuthResponseData);
    }

    protected var _data :AuthResponseData;
}
}