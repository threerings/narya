//
// $Id: SystemMessage.java 3098 2004-08-27 02:12:55Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A ChatMessage that represents a message that came from the server
 * and did not result from direct user action.
 */
public class SystemMessage extends ChatMessage
{
    /** Attention level constant to indicate that this message is merely
     * providing the user with information. */
    public static const INFO      :int = 0;

    /** Attention level constant to indicate that this message is the
     * result of a user action. */
    public static const FEEDBACK  :int = 1;

    /** Attention level constant to indicate that some action is required. */
    public static const ATTENTION :int = 2;

    /** The attention level of this message. */
    public var attentionLevel :int;

    public function SystemMessage (
            msg :String = null, bundle :String = null, attLevel :int = 0)
    {
        super(msg, bundle);
        this.attentionLevel = attLevel;
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        attentionLevel = ins.readByte();
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeByte(attentionLevel);
    }
}
}
