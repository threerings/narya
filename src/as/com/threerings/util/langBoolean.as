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

package com.threerings.util {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Equivalent to java.lang.Boolean.
 */
public class langBoolean
    implements Equalable, Streamable, Wrapped
{
    public var value :Boolean;

    public static function valueOf (val :Boolean) :langBoolean
    {
        return new langBoolean(val);
    }

    public function langBoolean (value :Boolean = false)
    {
        this.value = value;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is langBoolean) &&
            (value === (other as langBoolean).value);
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeBoolean(value);
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        value = ins.readBoolean();
    }

    // from Wrapped
    public function unwrap () :Object
    {
        return value;
    }
}
}
