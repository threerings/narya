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
 * A ChatMessage representing a message that came from another user.
 */
public class UserMessage extends ChatMessage
{
    /** The user that the message came from. */
    public var speaker :Name;

    /** The mode of the message. @see ChatCodes.DEFAULT_MODE */
    public var mode :int;

    /**
     * Constructs a user message for a player originated tell (which has no
     * bundle and is in the default mode).
     */
    public function UserMessage (speaker :Name = null, bundle :String = null,
                                 message :String = null, mode :int = 0)
    {
        super(message, bundle);
        this.speaker = speaker;
        this.mode = mode;
    }

    /**
     * Returns the name to display for the speaker.  Some types of messages
     *  may wish to not use the canonical name for the speaker and should thus
     *  override this function.
     */
    public function getSpeakerDisplayName () :Name
    {
        return speaker;
    }

    override public function getFormat () :String
    {
        switch (mode) {
        case ChatCodes.THINK_MODE: return "m.think_format";
        case ChatCodes.EMOTE_MODE: return "m.emote_format";
        case ChatCodes.SHOUT_MODE: return "m.shout_format";
        case ChatCodes.BROADCAST_MODE: return "m.broadcast_format";
        }

        if (ChatCodes.USER_CHAT_TYPE === localtype) {
            return "m.tell_format";
        }
        return "m.speak_format";
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        speaker = (ins.readObject() as Name);
        mode = ins.readByte();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(speaker);
        out.writeByte(mode);
    }
}
}
