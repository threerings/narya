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

package com.threerings.util {

/**
 * Contains sorting Comparators.
 * These functions are suitable for passing to Array.sort(), or with a flex Sort object.
 */
public class Comparators
{
    /**
     * A standard Comparator for comparing Comparable values.
     */
    public static function COMPARABLE (c1 :Comparable, c2 :Comparable, ... ignored) :int
    {
        if (c1 == c2) { // same object -or- both null
            return 0;
        } else if (c1 == null) {
            return -1;
        } else if (c2 == null) {
            return 1;
        } else {
            return c1.compareTo(c2);
        }
    }

    /**
     * Create a Comparator function that reverses the ordering of the specified Comparator.
     */
    public static function createReverse (comparator :Function) :Function
    {
        return function (o1 :Object, o2 :Object, ... ignored) :int {
            return comparator(o2, o1); // simply reverse the ordering
        }
    }
}
}
