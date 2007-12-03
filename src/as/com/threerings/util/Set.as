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

public interface Set
{
    /**
     * Adds the specified element to the set if it's not already present.
     * Returns true if the set did not already contain the specified element.
     */
    function add (o :Object) :Boolean;

    /**
     * Removes the specified element from this set if it is present.
     * Returns true if the set contained the specified element.
     */
    function remove (o :Object) :Boolean;

    /** Remove all elements from this set. */
    function clear () :void;

    /** Returns true if this set contains the specified element. */
    function contains (o :Object) :Boolean;

    /** Retuns the number of elements in this set. */
    function size () :int; // @TSC - should this be uint?

    /** Returns true if this set contains no elements. */
    function isEmpty () :Boolean;

    /**
     * Returns all elements in the set in an Array.
     * Modifying the returned Array will not modify the set.
     */
    function toArray () :Array;
}

}
