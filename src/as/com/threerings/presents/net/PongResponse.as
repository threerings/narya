package com.threerings.presents.net {

import com.threerings.util.long;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class PongResponse extends DownstreamMessage
{
    public function PongResponse ()
    {
        super();
    }

    public function getPackStamp () :long
    {
        return _packStamp;
    }

    public function getProcessDelay () :int
    {
        return _processDelay;
    }

    public function getUnpackStamp () :Number
    {
        return _unpackStamp;
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        _unpackStamp = new Date().getTime();
        super.readObject(ins);

        // TODO: Figure out how we're really going to cope with longs
        _packStamp = new long(0);
        _packStamp.readObject(ins);

        _processDelay = ins.readInt();
    }

    protected var _packStamp :long;

    protected var _processDelay :int;

    protected var _unpackStamp :Number;
}
}
