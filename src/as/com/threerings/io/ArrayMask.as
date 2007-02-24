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

package com.threerings.io {

import flash.utils.ByteArray;

public class ArrayMask
{
    public function ArrayMask (length :int = 0)
    {
        var mlength :int = int(length / 8);
        if (length % 8 != 0) {
            mlength++;
        }
        _mask.length = mlength;
    }

    /**
     * Set the specified index as containing a non-null element in the
     * array we're representing.
     */
    public function setBit (index :int) :void
    {
        _mask[int(index/8)] |= (1 << (index % 8));
    }

    /**
     * Is the specified array element non-null?
     */
    public function isSet (index :int) :Boolean
    {
        return (_mask[int(index/8)] & (1 << (index % 8))) != 0;
    }

    public function writeTo (out :ObjectOutputStream) :void
    {
        out.writeShort(_mask.length);
        out.writeBytes(_mask);
    }

    // documentation inherited from interface Streamable
    public function readFrom (ins :ObjectInputStream) :void
    {
        _mask.length = ins.readShort();
        ins.readBytes(_mask, 0, _mask.length);
    }

    /** The array mask. */
    protected var _mask :ByteArray = new ByteArray();
}
}
