//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
public final class langBoolean
    implements Hashable, Boxed, Streamable
{
    public static function valueOf (val :Boolean) :langBoolean
    {
        return new langBoolean(val);
    }

    /**
     * Access the immutable value.
     */
    public function get value () :Boolean
    {
        return _value;
    }

    /**
     * Constructor.
     */
    public function langBoolean (boolValue :Boolean = false)
    {
        _value = boolValue;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is langBoolean) &&
            (_value === (other as langBoolean).value);
    }

    // from Hashable
    public function hashCode () :int
    {
        return _value ? 1 : 0;
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeBoolean(_value);
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _value = ins.readBoolean();
    }

    // from Boxed
    public function unbox () :Object
    {
        return _value;
    }

    // cannot use the override keyword on toString() because actionscript is stupid
    public function toString () :String
    {
        return _value.toString();
    }

    protected var _value :Boolean;
}
}
