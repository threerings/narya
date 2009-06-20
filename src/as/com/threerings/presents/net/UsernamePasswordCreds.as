//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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
import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class UsernamePasswordCreds extends Credentials
{
    /**
     * Construct credentials with the supplied username and password.
     */
    public function UsernamePasswordCreds (username :Name = null, password :String = null)
    {
        _username = username;
        _password = password;
    }

    public function getUsername () :Name
    {
        return _username;
    }

    public function getPassword () :String
    {
        return _password;
    }

    // from Credentials
    override public function writeObject (out :ObjectOutputStream) :void
    {
    	out.writeObject(_username);
        out.writeField(_password);
    }

    // from Credentials
    override public function readObject (ins :ObjectInputStream) :void
    {
    	_username = Name(ins.readObject());
        _password = (ins.readField(String) as String);
    }

    // from Object
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder("[");
        toStringBuf(buf);
        buf.append("]");
        return buf.toString();
    }

    protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("username=", _username);
        buf.append(", password=", _password);
    }

    protected var _username :Name;
    protected var _password :String;
}
}
