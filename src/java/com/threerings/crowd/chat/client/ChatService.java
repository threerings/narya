//
// $Id: ChatService.java,v 1.5 2001/10/02 02:07:50 mdb Exp $

package com.threerings.cocktail.party.chat;

import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationDirector;
import com.threerings.cocktail.party.Log;

/**
 * The chat services provide a mechanism by which the client can broadcast
 * chat messages to all clients that are subscribed to a particular place
 * object or directly to a particular client. These services should not be
 * used directly, but instead should be accessed via the chat director.
 *
 * @see ChatDirector
 */
public class ChatService implements ChatCodes
{
    /**
     * Requests that a tell message be delivered to the user with username
     * equal to <code>target</code>.
     *
     * @param client a connected, operational client instance.
     * @param target the username of the user to which the tell message
     * should be delivered.
     * @param message the contents of the message.
     * @param rsptarget the chat director reference that will receive the
     * tell response.
     *
     * @return the invocation request id of the generated tell request.
     */
    public static int tell (Client client, String target, String message,
                            ChatDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { target, message };
        Log.info("Sending tell request [tgt=" + target +
                 ", msg=" + message + "].");
        return invdir.invoke(MODULE_NAME, TELL_REQUEST, args, rsptarget);
    }
}
