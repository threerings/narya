//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

import com.threerings.util.ClassUtil;
import com.threerings.util.Map;
import com.threerings.util.Maps;

/**
 * Streamer for Maps.
 */
public class MapStreamer extends Streamer
{
    public static const INSTANCE :MapStreamer = new MapStreamer();

    public static const DEFAULT_MAP :Map = Maps.newBuilder(Object).makeImmutable().build();

    public function MapStreamer ()
    {
        super(Map, "java.util.HashMap");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        var size :int = ins.readInt();
        var map :Map;
        if (size > 0) {
            // guess the type of map based on the first key
            var key :Object = ins.readObject();
            map = Maps.newMapOf(ClassUtil.getClass(key));
            map.put(key, ins.readObject());
            for (var ii :int = 1; ii < size; ii++) {
                map.put(ins.readObject(), ins.readObject());
            }
        } else {
            // oh crap
            map = DEFAULT_MAP;
        }
        return map;
    }

    override public function readObject (obj :Object, ins :ObjectInputStream) :void
    {
        // nada
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream) :void
    {
        var map :Map = Map(obj);
        out.writeInt(map.size());
        map.forEach(function (key :Object, value :Object) :void {
            out.writeObject(key);
            out.writeObject(value);
        });
    }
}
}
