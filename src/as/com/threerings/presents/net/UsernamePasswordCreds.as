//
// $Id: UsernamePasswordCreds.java 3099 2004-08-27 02:21:06Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.net {

import com.threerings.util.Name;

import com.threerrings.io.ObjectInputStream;
import com.threerrings.io.ObjectOutputStream;

public class UsernamePasswordCreds extends Credentials
{
    /**
     * Construct credentials with the supplied username and password.
     */
    public function UsernamePasswordCreds (username :Name, password :String)
    {
        super(username);
        _password = password;
    }

    public function getPassword () :String
    {
        return _password;
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(_password);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _password = ins.readField(String);
    }

    // documentation inherited
    protected override function toString (buf :StringBuilder)
    {
        super.toString(buf);
        buf.append(", password=", _password);
    }

    protected var _password :String;
}
}
