//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.Set;
import com.threerings.util.Sets;

/**
 * Streamer for Sets.
 */
public class SetStreamer extends Streamer
{
    public static const INSTANCE :SetStreamer = new SetStreamer();

    public static const DEFAULT_SET :Set = Sets.newBuilder(Object).makeImmutable().build();

    public function SetStreamer ()
    {
        super(Set, "java.util.HashSet");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        var size :int = ins.readInt();
        var set :Set;
        if (size > 0) {
            // guess the type of set based on the first value
            var first :Object = ins.readObject();
            set = Sets.newSetOf(ClassUtil.getClass(first));
            set.add(first);
            for (var ii :int = 1; ii < size; ii++) {
                set.add(ins.readObject());
            }
        } else {
            // oh crap
            set = DEFAULT_SET;
        }
        return set;
    }

    override public function readObject (obj :Object, ins :ObjectInputStream) :void
    {
        // nada
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream) :void
    {
        var set :Set = Set(obj);
        out.writeInt(set.size());
        set.forEach(function (value :Object) :void {
            out.writeObject(value);
        });
    }
}
}
