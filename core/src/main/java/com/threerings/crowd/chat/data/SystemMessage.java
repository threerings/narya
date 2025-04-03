//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

/**
 * A ChatMessage that represents a message that came from the server and did not result from direct
 * user action.
 */
public class SystemMessage extends ChatMessage
{
    /** Attention level constant to indicate that this message is merely providing the user with
     * information. */
    public static final byte INFO      = 0;

    /** Attention level constant to indicate that this message is the result of a user action. */
    public static final byte FEEDBACK  = 1;

    /** Attention level constant to indicate that some action is required. */
    public static final byte ATTENTION = 2;

    /** The attention level of this message. */
    public byte attentionLevel;

    /**
     * Construct a SystemMessage.
     */
    public SystemMessage (String message, String bundle, byte attentionLevel)
    {
        super(message, bundle);
        this.attentionLevel = attentionLevel;
    }
}
