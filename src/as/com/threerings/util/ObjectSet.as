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

import com.threerings.util.ObjectMap;
import com.threerings.util.Set;

public class ObjectSet
    implements Set
{
    /** @inheritDoc */
    public function add (o :Object) :Boolean
    {
        return (_objectMap.put(o, null) === undefined);
    }

    /** @inheritDoc */
    public function remove (o :Object) :Boolean
    {
        return (_objectMap.remove(o) !== undefined);
    }

    /** @inheritDoc */
    public function clear () :void
    {
        _objectMap.clear();
    }

    /** @inheritDoc */
    public function contains (o :Object) :Boolean
    {
        return _objectMap.containsKey(o);
    }

    /** @inheritDoc */
    public function size () :int
    {
        return _objectMap.size();
    }

    /** @inheritDoc */
    public function isEmpty () :Boolean
    {
        return _objectMap.isEmpty();
    }

    /** @inheritDoc */
    public function toArray () :Array
    {
        return _objectMap.keys();
    }

    /** @inheritDoc */
    public function forEach (callback :Function) :void
    {
        _objectMap.forEach(function (key :Object, ...ignored) :void {
            callback(key);
        });
    }

    protected var _objectMap :ObjectMap = new ObjectMap();
}

}
