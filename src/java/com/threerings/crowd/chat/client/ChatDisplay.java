//
// $Id: ChatDisplay.java,v 1.14 2002/08/14 00:48:57 shaper Exp $

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
     */
    public void clear ();

    /**
     * Called to display a chat message.
     *
     * @see ChatMessage
     */ 
    public void displayMessage (ChatMessage msg);
}
