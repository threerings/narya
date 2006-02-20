package com.threerings.presents.net {

import com.threerings.presents.dobj.DObject;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class AuthResponseData extends DObject
{
    /** A constant used to indicate a successful authentication. */
    public static const SUCCESS :String = "success";

    /** Either the SUCCESS constant or a reason code indicating
     * why the authentication failed. */
    public var code :String;

    // documentation inherited
    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(code);
    }

    // documentation inherited
    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        code = (ins.readField(String) as String);
    }
}
}
