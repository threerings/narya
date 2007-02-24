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

/**
 * Equivalent to java.lang.Long.
 */
public class Long
    implements Equalable
{
    public var high :int;
    public var low :int;

    public static function valueOf (low :int, high :int = 0) :Long
    {
        return new Long(low, high);
    }

    public function Long (low :int, high :int = 0)
    {
        this.low = low;
        this.high = high;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        if (!(other is Long)) {
            return false;
        }
        var that :Long = (other as Long);
        return (this.high == that.high) && (this.low == that.low);
    }

    // from Wrapped, except that we don't implement Wrapped anymore
    public function unwrap () :Object
    {
        return low; // TODO
    }
}
}
