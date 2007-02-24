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

package com.threerings.io.streamers {

import flash.utils.ByteArray;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for ByteArray objects.
 */
public class ByteArrayStreamer extends Streamer
{
    public function ByteArrayStreamer ()
    {
        super(ByteArray, "[B"); // yes, that's the Java class for a byte[].
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        var bytes :ByteArray = new ByteArray();
        bytes.length = ins.readInt();
        return bytes;
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        var bytes :ByteArray = (obj as ByteArray);
        ins.readBytes(bytes, 0, bytes.length);
    }
}
}
