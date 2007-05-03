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
 * Equivalent to java.lang.Integer.
 */
// Unfortunately, I think this is necessary.
// I was going to remove this class and just make the streaming stuff
// autotranslate between int <--> java.lang.Integer and
// Number <--> java.lang.Double. However, a Number object that refers
// to an integer value is actually an int. Yes, it's totally fucked.
public class Integer
    implements Equalable, Wrapped
{
    public var value :int;

    public static function valueOf (val :int) :Integer
    {
        return new Integer(val);
    }

    public function Integer (value :int)
    {
        this.value = value;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Integer) && (value === (other as Integer).value);
    }

    // from Wrapped
    public function unwrap () :Object
    {
        return value;
    }

    // cannot use the override keyword on toString() because actionscript is stupid
    public function toString () :String
    {
        return value.toString();
    }

    /**
     * Compares to int values in an overflow safe manner.
     */
    public static function compare (val1 :int, val2 :int) :int
    {
        return (val1 > val2) ? 1 : (val1 == val2 ? 0 : -1);
    }
}
}
