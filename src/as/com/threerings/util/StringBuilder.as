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

public class StringBuilder
{
    public function StringBuilder (... args)
    {
        append.apply(this, args);
    }

    /**
     * Append all arguments to the end of the string being built
     * and return this StringBuilder.
     */
    public function append (... args) :StringBuilder
    {
        for each (var o :Object in args) {
            _buf += String(o);
        }
        return this;
    }

    /**
     * Return the String built so far.
     */
    public function toString () :String
    {
        // it's ok, Strings are immutable
        return _buf;
    }

    /** The string upon which we build. Internally in AVM2, Strings have
     * been designed with a prefix pointer so that concatination is
     * really cheap. */
    protected var _buf :String = "";
}
}
