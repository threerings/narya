package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthRequest extends UpstreamMessage
{
    public function AuthRequest ()
    {
        super();
    }

    public function AuthRequest (creds :Credentials, version :String)
    {
        super();
        _creds = creds;
        _version = version;
    }

    // documentation inherited
    public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeObject(_creds);
        out.writeField(_version);
    }

    // documentation inherited
    public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _creds = ins.readObject();
        _version = ins.readField(String);
    }

    protected var _creds :Credentials;
    protected var _version :String;
}
}
