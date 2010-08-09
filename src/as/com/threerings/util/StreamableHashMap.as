//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.Maps;
import com.threerings.util.maps.ForwardingMap;
import com.threerings.util.maps.HashMap;
import com.threerings.util.maps.ImmutableMap;

/**
 * A Map that can be sent over the wire, bearing in mind that all keys and values must
 * be primitives or implement Streamable.
 *
 * @see com.threerings.util.Map
 * @see com.threerings.io.Streamable
 */
public class StreamableHashMap extends ForwardingMap
    implements Streamable
{
    /**
     * Creates a new StreamableHashMap.
     *
     * @param keyClazz The class to use as the map key. May be null, but this functionality is
     * provided only for deserialization. A StreamableHashMap that has not been initialized
     * properly by either a non-null keyClazz or via readObject will throw errors on write and
     * always return null on read.
     */
    public function StreamableHashMap (keyClazz :Class = null)
    {
        super(keyClazz == null ? DEFAULT_MAP : Maps.newMapOf(keyClazz));
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(size());
        forEach(function (key :Object, value :Object) :void {
            out.writeObject(key);
            out.writeObject(value);
        });
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        var ecount :int = ins.readInt();
        if (ecount > 0) {
            // guess the type of map based on the first key
            var key :Object = ins.readObject();
            _source = Maps.newMapOf(ClassUtil.getClass(key));
            put(key, ins.readObject());
            // now read the rest
            for (var ii :int = 1; ii < ecount; ii++) {
                put(ins.readObject(), ins.readObject());
            }

        } else {
            _source = Maps.newMapOf(String); // hope for the best
        }
    }

    protected static const DEFAULT_MAP :Map = new ImmutableMap(new HashMap());
}
}
