//
// $Id: ChatDisplay.java,v 1.17 2003/06/03 21:41:33 ray Exp $

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
    public void clear ();

    /**
     * Called to display a chat message.
     *
     * @see ChatMessage
     */ 
    public void displayMessage (ChatMessage msg);
}
