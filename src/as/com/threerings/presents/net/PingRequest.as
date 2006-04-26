package com.threerings.presents.net {

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
    public override function writeObject (out :ObjectOutputStream) :void
    {
        _packStamp = flash.util.getTimer();
        super.writeObject(out);
    }

    /** A time stamp obtained when we serialize this object. */
    protected var _packStamp :uint;
}
}
