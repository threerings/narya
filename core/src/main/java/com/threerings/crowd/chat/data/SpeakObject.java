//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

import com.threerings.crowd.chat.data.UserMessage;

/**
 * Provides a mechanism by which the speak service can identify chat listeners so as to maintain a
 * recent history of all chat traffic on the server.
 */
public interface SpeakObject
{
    public static final String DEFAULT_IDENTIFIER = "default";

    /** Used in conjunction with {@link SpeakObject#applyToListeners}. */
    public static interface ListenerOp
    {
        /** Call this method if you only have access to body oids. */
        void apply (SpeakObject sender, int bodyOid);

        /** Call this method if you can provide usernames directly. */
        void apply (SpeakObject sender, Name username);
    }

    /**
     * Returns an identifier for what type of chat this speak object represents based on the message.
     */
    default String getChatIdentifier (UserMessage message) {
        return DEFAULT_IDENTIFIER;
    }

    /**
     * The speak service will call this every time a chat message is delivered on this speak object
     * to note the listeners that received the message.
     */
    void applyToListeners (ListenerOp op);
}
