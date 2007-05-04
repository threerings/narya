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

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

/**
 * A system message triggered by the activity of another user. If the user is muted we can suppress
 * this message, unlike a normal system message.
 */
public class UserSystemMessage extends SystemMessage
{
    /** The "speaker" of this message, the user that triggered that this message be sent to us. */
    public Name speaker;

    /** Suitable for unserialization. */
    public UserSystemMessage ()
    {
    }

    /**
     * Construct a INFO-level UserSystemMessage.
     */
    public UserSystemMessage (Name sender, String message, String bundle)
    {
        this(sender, message, bundle, INFO);
    }

    /**
     * Construct a UserSystemMessage.
     */
    public UserSystemMessage (Name sender, String message, String bundle, byte attentionLevel)
    {
        super(message, bundle, attentionLevel);
        this.speaker = sender;
    }
}
