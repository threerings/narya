//
// $Id: ChatService.java,v 1.2 2001/08/02 23:46:47 mdb Exp $

package com.threerings.cocktail.party.chat;

/**
 * The chat services provide a mechanism by which the client can broadcast
 * chat messages to all clients that are subscribed to a particular place
 * object or directly to a particular client. These services should not be
 * used directly, but instead should be accessed via the chat manager.
 *
 * @see ChatManager
 */
public class ChatService
{
    /** The module name for the chat services. */
    public static final String MODULE = "chat";

    /** The message identifier for a speak request message. */
    public static final String SPEAK_REQUEST = "spkreq";

    /** The message identifier for a speak notification message. */
    public static final String SPEAK_NOTIFICATION = "spknot";
}
