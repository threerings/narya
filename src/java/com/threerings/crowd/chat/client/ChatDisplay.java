//
// $Id: ChatDisplay.java,v 1.15 2002/10/27 22:33:42 ray Exp $

package com.threerings.crowd.chat;

/**
 * A chat display provides a means by which chat messages can be
 * displayed. The chat display will be notified when chat messages of
 * various sorts have been received by the client.
 */
public interface ChatDisplay
{
    /**
     * Called to clear the chat display.
     *
     * @param force if false, the ChatDisplay can choose to ignore the clear.
     */
    public void clear (boolean force);

    /**
     * Called to display a chat message.
     *
     * @see ChatMessage
     */ 
    public void displayMessage (ChatMessage msg);
}
