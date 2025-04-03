//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
     * Constructs a user message for a player originated tell (which has no bundle and is in the
     * default mode).
     */
    public static UserMessage create (Name speaker, String message)
    {
        return new UserMessage(speaker, null, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Construct a user message.
     */
    public UserMessage (Name speaker, String bundle, String message, byte mode)
    {
        super(message, bundle);
        this.speaker = speaker;
        this.mode = mode;
    }

    /**
     * Returns the name to display for the speaker.  Some types of messages may wish to not use the
     * canonical name for the speaker and should thus override this function.
     */
    public Name getSpeakerDisplayName ()
    {
        return speaker;
    }

    @Override
    public String getFormat ()
    {
        switch (mode) {
        case ChatCodes.THINK_MODE: return "m.think_format";
        case ChatCodes.EMOTE_MODE: return "m.emote_format";
        case ChatCodes.SHOUT_MODE: return "m.shout_format";
        case ChatCodes.BROADCAST_MODE: return "m.broadcast_format";
        default: // fall through
        }

        if (ChatCodes.USER_CHAT_TYPE.equals(localtype)) {
            return "m.tell_format";
        }
        return "m.speak_format";
    }
}
