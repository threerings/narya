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
 * Equivalent to java.lang.Short.
 */
public class Short
    implements Equalable, Wrapped
{
    /** The minimum possible short value. */
    public static const MIN_VALUE :int = -Math.pow(2, 15);

    /** The maximum possible short value. */
    public static const MAX_VALUE :int = (Math.pow(2, 15) - 1);

    /** The value of this short. */
    public var value :int;

    public static function valueOf (val :int) :Short
    {
        return new Short(val);
    }

    public function Short (value :int)
    {
        this.value = value;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Short) && (value === (other as Short).value);
    }

    // from Wrapped
    public function unwrap () :Object
    {
        return value;
    }
}
}
