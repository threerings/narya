//
// $Id: ChatDisplay.java,v 1.13 2002/07/26 20:35:01 ray Exp $

package com.threerings.crowd.chat;

/**
 * A chat display provides a means by which chat messages can be
 * displayed. The chat display will be notified when chat messages of
 * various sorts have been received by the client.
 */
public interface ChatDisplay
{
    /**
     * Called to display a message.
     * @see ChatMessage
     */ 
    public void displayMessage (ChatMessage msg);
}
