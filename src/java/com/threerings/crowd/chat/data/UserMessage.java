//
// $Id$
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

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

/**
 * A ChatMessage representing a message that came from another user.
 */
public class UserMessage extends ChatMessage
{
    /** The user that the message came from. */
    public Name speaker;

    /** The mode of the message. @see ChatCodes.DEFAULT_MODE */
    public byte mode;

    /**
     * For unserialization.
     */
    public UserMessage ()
    {
    }

    /**
     * Construct a user message.
     */
    public UserMessage (String message, String bundle, Name speaker, byte mode)
    {
        super(message, bundle);
        this.speaker = speaker;
        this.mode = mode;
    }

    /**
     * Constructs a user message for a player originated tell (which has no
     * bundle and is in the default mode).
     */
    public UserMessage (String message, Name speaker)
    {
        super(message, null);
        this.speaker = speaker;
        this.mode = ChatCodes.DEFAULT_MODE;
    }
}
