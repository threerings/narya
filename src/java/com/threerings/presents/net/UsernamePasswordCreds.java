//
// $Id: UsernamePasswordCreds.java,v 1.14 2004/08/27 02:20:22 mdb Exp $
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

package com.threerings.presents.net;

import com.threerings.util.Name;

public class UsernamePasswordCreds extends Credentials
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UsernamePasswordCreds ()
    {
        super();
    }

    /**
     * Construct credentials with the supplied username and password.
     */
    public UsernamePasswordCreds (Name username, String password)
    {
        super(username);
        _password = password;
    }

    public String getPassword ()
    {
        return _password;
    }

    // documentation inherited
    public int hashCode ()
    {
        return super.hashCode() ^ _password.hashCode();
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof UsernamePasswordCreds) {
            UsernamePasswordCreds upcreds = (UsernamePasswordCreds)other;
            return super.equals(other) &&
                _password.equals(upcreds._password);
        } else {
            return false;
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", password=").append(_password);
    }

    protected String _password;
}
