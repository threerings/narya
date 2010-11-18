//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
import com.threerings.io.streamers.MapStreamer;

import com.threerings.util.Maps;
import com.threerings.util.maps.ForwardingMap;

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
        super(keyClazz == null ? MapStreamer.DEFAULT_MAP : Maps.newMapOf(keyClazz));
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        MapStreamer.INSTANCE.writeObject(this, out);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _source = Map(MapStreamer.INSTANCE.createObject(ins));
    }
}
}
