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

import flash.errors.IllegalOperationError;

/**
 * Provides a generic iterator for an Array.
 * No co-modification checking is done.
 */
public class ArrayIterator
    implements Iterator
{
    /**
     * Create an ArrayIterator.
     */
    public function ArrayIterator (arr :Array, allowRemove :Boolean = true)
    {
        _arr = arr;
        _index = 0;
        _lastIndex = allowRemove ? -1 : -2;
    }

    // documentation inherited from interface Iterator
    public function hasNext () :Boolean
    {
        return (_index < _arr.length);
    }

    // documentation inherited from interface Iterator
    public function next () :Object
    {
        if (_lastIndex != -2) {
            _lastIndex = _index;
        }
        return _arr[_index++];
    }

    // documentation inherited from interface Iterator
    public function remove () :void
    {
        if (_lastIndex < 0) {
            throw new IllegalOperationError();

        } else {
            _arr.splice(_lastIndex, 1);
            _lastIndex = -1;
            _index--; // since _lastIndex is always before _index
        }
    }

    /** The array we're iterating over. */
    protected var _arr :Array;

    /** The current index. */
    protected var _index :int;

    /** The last-removed index.
     * Or -1 for already removed, -2 for no removals allowed. */
    protected var _lastIndex :int;
}
}
