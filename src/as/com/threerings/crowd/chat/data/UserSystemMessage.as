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

package com.threerings.crowd.chat.data {

import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A system message triggered by the activity of another user.
 * If the user is muted we can suppress this message, unlike a normal
 * system message.
 */
public class UserSystemMessage extends SystemMessage
{
    /** The "speaker" of this message, the user that triggered that this
     * message be sent to us. */
    public var speaker :Name;

    /**
     * Construct a UserSystemMessage.
     */
    public function UserSystemMessage (
        sender :Name = null, message :String = null, bundle :String = null,
        attLevel :int = 0)
    {
        super(message, bundle, attLevel);
        this.speaker = sender;
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        speaker = (ins.readObject() as Name);
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(speaker);
    }
}
}
