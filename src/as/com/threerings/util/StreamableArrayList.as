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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class StreamableArrayList
    implements Streamable
{
    public function StreamableArrayList (values :Array = null)
    {
        _array = (values == null) ? new Array() : values;
    }

    public function add (item :Object, index :int = int.MAX_VALUE) :Boolean
    {
        _array.splice(Math.min(_array.length, index), 0, item);
        return true;
    }

    /**
     * Add all the elements in the other list.
     *
     * @param otherList may be another StreamableArrayList or an Array
     */
    public function addAll (otherList :*, index :int = int.MAX_VALUE) :Boolean
    {
        var other :Array = getArray(otherList);

        // splice the array in at the index
        _array.splice(Math.min(_array.length, index), 0, other);
        return (other.length > 0);
    }

    public function clear () :void
    {
        _array.length = 0;
    }

    public function contains (item :Object) :Boolean
    {
        return ArrayUtil.contains(_array, item);
    }

    public function get (index :int) :*
    {
        return _array[index];
    }

    public function indexOf (item :Object) :int
    {
        return ArrayUtil.indexOf(_array, item);
    }

    public function isEmpty () :Boolean
    {
        return (_array.length == 0);
    }

    public function iterator () :Iterator
    {
        return new ArrayIterator(_array);
    }

    public  function remove (item :Object) :Boolean
    {
        return ArrayUtil.removeFirst(_array, item);
    }

    public function removeAll (otherList :*) :Boolean
    {
        var other :Array = getArray(otherList);

        var removed :Boolean = false;
        for (var ii :int = other.length - 1; ii >= 0; ii--) {
            if (remove(other[ii])) {
                removed = true;
            }
        }
        return removed;
    }

    public function removeAt (index :int) :Object
    {
        return _array.splice(index, 1)[0];
    }

    public function set (index :int, item :Object) :Object
    {
        var ret :Object = _array[index];
        _array[index] = item;
        return ret;
    }

    public function size () :int
    {
        return _array.length;
    }

    /**
     * Return the actual array storage.
     */
    public function asArray () :Array
    {
        return _array;
    }

    /**
     * Return a copy of the internal storage array.
     */
    public function toArray () :Array
    {
        return _array.concat();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        var len :int = _array.length;
        out.writeInt(len);
        for (var ii :int = 0; ii < len; ii++) {
            out.writeObject(_array[ii]);
        }
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        var len :int = ins.readInt();
        _array.length = len; // truncate (or grow once)
        for (var ii :int = 0; ii < len; ii++) {
            _array[ii] = ins.readObject();
        }
    }

    /**
     * Return an array representing the argument.
     */
    protected function getArray (otherList :*) :Array
    {
        if (otherList is StreamableArrayList) {
            return (otherList as StreamableArrayList).asArray();

        } else if (otherList is Array) {
            return (otherList as Array);

        } else {
            throw new ArgumentError();
        }
    }

    /** Storage. */
    protected var _array :Array;
}
}
