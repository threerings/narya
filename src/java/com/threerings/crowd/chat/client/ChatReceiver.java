//
// $Id: ChatReceiver.java,v 1.3 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.client.InvocationReceiver;

/**
 * Defines, for the chat services, a set of notifications delivered
 * asynchronously by the server to the client.
 */
public interface ChatReceiver extends InvocationReceiver
{
    /**
     * Called when a tell message is received from another player on the
     * server.
     *
     * @param speaker the username of the user from which this message
     * originated.
     * @param bundle if non-null, a bundle that should be used to
     * translate the text of the tell message. This is generally only used
     * when some server entity originates the tell message rather than
     * another user. The server entity might then wish for its tell
     * message to be translated into a language appropriate for the
     * receiver. Such luxuries are not available for human to human
     * conversation, alas.
     * @param message the text of the tell message.
     */
    public void receivedTell (String speaker, String bundle, String message);
}
