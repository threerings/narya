package com.threerings.presents.net {

import flash.utils.getTimer;

import com.threerings.io.ObjectOutputStream;

public class PingRequest extends UpstreamMessage
{
    /** The number of milliseconds of idle upstream that are allowed to elapse
     * before the client sends a ping message to the server to let it
     * know that we're still alive. */
    public static const PING_INTERVAL :uint = 60 * 1000;

    public function PingRequest ()
    {
        super();
    }

    public function getPackStamp () :uint
    {
        return _packStamp;
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        _packStamp = getTimer();
        super.writeObject(out);
    }

    /** A time stamp obtained when we serialize this object. */
    protected var _packStamp :uint;
}
}
