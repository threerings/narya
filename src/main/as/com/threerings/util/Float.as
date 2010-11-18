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
 * Equivalent to java.lang.Float.
 */
public final class Float
    implements Hashable, Boxed, Streamable
{
    public static function valueOf (val :Number) :Float
    {
        return new Float(val);
    }

    /**
     * Access the immutable value.
     */
    public function get value () :Number
    {
        return _value;
    }

    /**
     * Constructor.
     */
    public function Float (floatValue :Number = 0)
    {
        _value = floatValue;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Float) && (_value === (other as Float).value);
    }

    // from Hashable
    public function hashCode () :int
    {
        return int(_value);
    }

    // from Boxed
    public function unbox () :Object
    {
        return _value;
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _value = ins.readFloat();
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeFloat(_value);
    }

    protected var _value :Number;
}
}
