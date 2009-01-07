//
// $Id$

package com.threerings.crowd.chat.client {

import com.threerings.crowd.chat.data.ChatMessage;

/**
 * An interface for listening to chat received from the server, prior
 * to it being filtered/muted/whatever.
 */
public interface ChatSnooper
{
    /**
     * Handle the arrival of chat. Do not modify the chat message! That would be a filter!
     */
    function snoopChat (msg :ChatMessage) :void;
}
}
