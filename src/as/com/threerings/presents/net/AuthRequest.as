package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthRequest extends UpstreamMessage
{
    public function AuthRequest (creds :Credentials, version :String)
    {
        _creds = creds;
        _version = version;
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeObject(_creds);
        out.writeField(_version);
    }

    // documentation inherited
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        _creds = (ins.readObject() as Credentials);
        _version = (ins.readField(String) as String);
    }

    protected var _creds :Credentials;
    protected var _version :String;
}
}
