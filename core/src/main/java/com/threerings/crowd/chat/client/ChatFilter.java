//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import com.threerings.util.Name;

/**
 * Filters messages chat messages to or from the server.
 */
public interface ChatFilter
{
    /**
     * Filter a chat message.
     * @param msg the message text to be filtered.
     * @param otherUser an optional argument that represents the target or the speaker, depending
     * on 'outgoing', and can be considered in filtering if it is provided.
     * @param outgoing true if the message is going out to the server.
     *
     * @return the filtered message, or null to block it completely.
     */
    String filter (String msg, Name otherUser, boolean outgoing);
}
