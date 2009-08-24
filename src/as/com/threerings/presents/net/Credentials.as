//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public /* abstract */ class Credentials
    implements Streamable
{
    public function Credentials ()
    {
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        // nada
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        throw new Error(); // we never read Creds on the client
    }

    /*
    // main
    public function toString (joiner :Joiner = null) :String
    {
        if (joiner == null) {
            return toString(new Joiner("ClassName"));
        }
        return joiner.add("baz", bit, "count", 33).toString();
    }

    // subclass
    override public function toString (joiner :Joiner = null) :String
    {
        if (joiner != null) {
            joiner.add("foo", bar);
        }
        return super.toString(joiner);
    }


    // -----

    public function toString () :String
    {
        return join(new Joiner(this)).toString();
    }

    protected function join (joiner :Joiner) :Joiner
    {
        return joiner.add(
            "baz", bit, "count", 33);
    }

    override protected join (joiner :Joiner) :Joiner
    {
        return super.join(joiner).add(
            "foo", bar);
    }
    */
}
}
