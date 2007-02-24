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

import com.threerings.util.Equalable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class Name extends Object
    implements Comparable, Hashable, Streamable
{
    public function Name (name :String = "")
    {
        _name = name;
    }

    public function getNormal () :String
    {
        if (_normal == null) {
            _normal = normalize(_name);
            if (_normal === _name) {
                _normal = _name;
            }
        }
        return _normal;
    }

    public function isValid () :Boolean
    {
        return !isBlank();
    }

    public function isBlank () :Boolean
    {
        return Name.isBlank(this);
    }

    public function toString () :String
    {
        return _name;
    }

    // from interface Hashable
    public function equals (other :Object) :Boolean
    {
        return (other != null) && ClassUtil.isSameClass(other, this) &&
            (getNormal() === (other as Name).getNormal());
    }

    // from interface Hashable
    public function hashCode () :int
    {
        return StringUtil.hashCode(getNormal());
    }

    // from interface Comparable
    public function compareTo (other :Object) :int
    {
        var thisNormal :String = getNormal();
        var thatNormal :String = (other as Name).getNormal();
        return thisNormal.localeCompare(thatNormal);
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        out.writeField(_name);
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        _name = (ins.readField(String) as String);
    }

    protected function normalize (txt :String) :String
    {
        return txt.toLowerCase();
    }

    public static function isBlank (name :Name) :Boolean
    {
        return (name == null || "" === name.toString());
    }

    /** The raw name text. */
    protected var _name :String;

    /** The normalized name text. */
    protected var _normal :String;
}
}
