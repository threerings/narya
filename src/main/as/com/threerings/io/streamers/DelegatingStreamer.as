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

/**
 * A streamer that allows subclasses or implementations of the classes or interfaces supported by
 * other streamers to be included in upstream messages. All serialization support is impelemented
 * using the parent streamer, but the java class name is different. This is so that the streamer
 * caching mechanism and the upstream class mappings both work.
 */
public class DelegatingStreamer extends Streamer
{
    public function DelegatingStreamer (parent :Streamer, targ:Class, jname:String=null)
    {
        super(targ, jname);
        _parent = parent;
    }

    /** @inheritDoc */
    override public function getUpstreamJavaClassName () :String
    {
        return _parent.getUpstreamJavaClassName();
    }

    /** @inheritDoc */
    override public function writeObject (obj :Object, out :ObjectOutputStream) :void
        //throws IOError
    {
        _parent.writeObject(obj, out);
    }

    /** @inheritDoc */
    override public function createObject (ins :ObjectInputStream) :Object
        //throws IOError
    {
        return _parent.createObject(ins);
    }

    /** @inheritDoc */
    override public function readObject (obj :Object, ins :ObjectInputStream) :void
        //throws IOError
    {
        _parent.readObject(obj, ins);
    }

    protected var _parent :Streamer;
}
}
