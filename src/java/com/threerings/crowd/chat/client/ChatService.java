//
// $Id: ChatService.java,v 1.3 2001/08/04 02:54:28 mdb Exp $

package com.threerings.cocktail.party.chat;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationManager;
import com.threerings.cocktail.party.Log;

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

    /** The message identifier for a tell request. */
    public static final String TELL_REQUEST = "Tell";

    /** The message identifier for a tell notification. */
    public static final String TELL_NOTIFICATION = "Tell";

    /**
     * Requests that a tell message be delivered to the user with username
     * equal to <code>target</code>.
     *
     * @param client a connected, operational client instance.
     * @param target the username of the user to which the tell message
     * should be delivered.
     * @param message the contents of the message.
     * @param rsptarget the chat manager reference that will receive the
     * tell response.
     *
     * @return the invocation request id of the generated tell request.
     */
    public static int tell (Client client, String target, String message,
                            ChatManager rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[] { target, message };
        Log.info("Sending tell request [tgt=" + target +
                 ", msg=" + message + "].");
        return invmgr.invoke(MODULE, TELL_REQUEST, args, rsptarget);
    }
}
