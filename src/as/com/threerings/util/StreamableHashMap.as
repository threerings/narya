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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Log;
import com.threerings.util.Maps;
import com.threerings.util.maps.ForwardingMap;

/**
 * A @see com.threerings.util.Map that can be sent over the wire,
 * bearing in mind that all keys and values must
 * be primitives or implement @see com.threerings.io.Streamable
 */
public class StreamableHashMap extends ForwardingMap
    implements Streamable
{
    public function StreamableHashMap (keyClazz :Class = null)
    {
        if (keyClazz != null) {
            super(Maps.newMapOf(keyClazz));
        }
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
            // shit!
            Log.getLog(this).warning("Empty StreamableHashMap read, guessing DictionaryMap.");
            _source = Maps.newMapOf(Object);
        }
    }
}
}
