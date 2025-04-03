//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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

    /**
     * Construct a INFO-level UserSystemMessage.
     */
    public static UserSystemMessage create (Name sender, String message, String bundle)
    {
        return new UserSystemMessage(sender, message, bundle, INFO);
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
