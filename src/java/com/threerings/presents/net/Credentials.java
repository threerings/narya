//
// $Id$
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

import com.threerings.io.Streamable;
import com.threerings.util.Name;

/**
 * Credentials are supplied by the client implementation and sent along to
 * the server during the authentication process. To provide support for a
 * variety of authentication methods, the credentials class is meant to be
 * subclassed for the particular method (ie. password, auth digest, etc.)
 * in use in a given system.
 *
 * <p> All credentials must provide a username as the username is used to
 * associate network connections with sessions.
 *
 * <p> All derived classes should provide a no argument constructor so
 * that they can be instantiated prior to reconstruction from a data input
 * stream.
 */
public abstract class Credentials implements Streamable
{
    /**
     * Constructs a credentials instance with the specified username.
     */
    public Credentials (Name username)
    {
        _username = username;
    }

    /**
     * Constructs a blank credentials instance in preparation for
     * unserializing from the network.
     */
    public Credentials ()
    {
    }

    public Name getUsername ()
    {
        return _username;
    }

    public void setUsername (Name name)
    {
        _username = name;
    }

    // documentation inherited
    public int hashCode ()
    {
        return _username.hashCode();
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof Credentials) {
            return _username.equals(((Credentials)other)._username);
        } else {
            return false;
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An easily extensible method via which derived classes can add to
     * {@link #toString()}'s output.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("username=").append(_username);
    }

    protected Name _username;
}
