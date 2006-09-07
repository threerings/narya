package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthRequest extends UpstreamMessage
{
    public function AuthRequest (creds :Credentials, version :String)
    {
        _creds = creds;
        _version = version;

        // magic up a timezone in the format "GMT+XX:XX"
        // Of course, the sign returned from getTimezoneOffset() is wrong
        var minsOffset :int = -1 * new Date().getTimezoneOffset();
        var hoursFromUTC :int = Math.abs(minsOffset) / 60;
        var minsFromUTC :int = Math.abs(minsOffset) % 60;

        minsFromUTC %= 60;
        _zone = "GMT" + ((minsOffset < 0) ? "-" : "+") +
            ((hoursFromUTC < 10) ? "0" : "") + hoursFromUTC + ":" +
            ((minsFromUTC < 10) ? "0" : "") + minsFromUTC;
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeObject(_creds);
        out.writeField(_version);
        out.writeField(_zone);
    }

    // documentation inherited
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        _creds = (ins.readObject() as Credentials);
        _version = (ins.readField(String) as String);
        _zone = (ins.readField(String) as String);
    }

    protected var _creds :Credentials;
    protected var _version :String;
    protected var _zone :String;
}
}
