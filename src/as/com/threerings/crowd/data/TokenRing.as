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

package com.threerings.crowd.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

/**
 * Defines access control tokens that convey certain privileges to users
 * (see {@link BodyObject#checkAccess}).
 */
public class TokenRing extends SimpleStreamableObject
{
    /** Indicates that this user is an administrator and can do things
     * like broadcast, shutdown the server and whatnot. */
    public static const ADMIN :int = (1 << 0);

    /**
     * Constructs a token ring with the supplied set of tokens.
     */
    public function TokenRing (tokens :int = 0)
    {
        _tokens = tokens;
    }

    /**
     * Adds the specified token to this ring.
     */
    public function setToken (token :int, setOn :Boolean = true) :void
    {
        if (setOn) {
            _tokens |= token;

        } else {
            clearToken(token);
        }
    }

    /**
     * Returns the bitmask that stores the various tokens.
     */
    public function getTokens () :int
    {
        return _tokens;
    }

    /**
     * Convenience function for checking whether this ring holds the
     * {@link #ADMIN} token.
     */
    public function isAdmin () :Boolean
    {
        return holdsToken(ADMIN);
    }

    /**
     * Returns true if this token ring contains the specified token or tokens,
     * exactly.
     * For example, if you pass in the OR of two or more tokens,
     * then the ring must contain all of those tokens.
     */
    public function holdsToken (token :int) :Boolean
    {
        return (_tokens & token) == token;
    }

    /**
     * Returns true if this token ring contains any one of the specified tokens.
     */
    public function holdsAnyToken (tokens :int) :Boolean
    {
        return (_tokens & tokens) != 0;
    }

    /**
     * Clears the specified token from this ring.
     */
    public function clearToken (token :int) :void
    {
        _tokens &= ~token;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _tokens = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(_tokens);
    }

    /** The tokens contained in this ring (composed together bitwise). */
    protected var _tokens :int;
}
}
