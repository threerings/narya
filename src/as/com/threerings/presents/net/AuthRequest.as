//
// $Id$

package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.util.StringUtil;

public class AuthRequest extends UpstreamMessage
{
    public function AuthRequest (creds :Credentials, version :String, bootGroups :Array)
    {
        _creds = creds;
        _version = version;
        _bootGroups = TypedArray.create(String);
        _bootGroups.addAll(bootGroups);

        // magic up a timezone in the format "GMT+XX:XX"
        // Of course, the sign returned from getTimezoneOffset() is wrong
        var minsOffset :int = -1 * new Date().getTimezoneOffset();
        var hoursFromUTC :int = Math.abs(minsOffset) / 60;
        var minsFromUTC :int = Math.abs(minsOffset) % 60;
        _zone = "GMT" + ((minsOffset < 0) ? "-" : "+") +
            StringUtil.prepad(String(hoursFromUTC), 2, "0") + ":" +
            StringUtil.prepad(String(minsFromUTC), 2, "0");
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeObject(_creds);
        out.writeField(_version);
        out.writeField(_zone);
        out.writeObject(_bootGroups);
    }

    protected var _creds :Credentials;
    protected var _version :String;
    protected var _zone :String;
    protected var _bootGroups :TypedArray;
}
}
