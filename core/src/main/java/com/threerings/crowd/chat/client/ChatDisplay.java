//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import com.threerings.crowd.chat.data.ChatMessage;

/**
 * A chat display provides a means by which chat messages can be
 * displayed. The chat display will be notified when chat messages of
 * various sorts have been received by the client.
 */
public interface ChatDisplay
{
    /**
     * Called to clear the chat display.
     */
    void clear ();

    /**
     * Called to display a chat message.
     *
     * @param alreadyDisplayed true if a previous chat display in the list has
     * already displayed this message, false otherwise.
     *
     * @return true if the message was displayed, false if not.
     */
    boolean displayMessage (ChatMessage msg, boolean alreadyDisplayed);
}
