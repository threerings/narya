//
// $Id: ChatProvider.java,v 1.4 2001/10/01 22:14:55 mdb Exp $

package com.threerings.cocktail.party.chat;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.server.PartyServer;

/**
 * The chat provider handles the server side of the chat-related
 * invocation services.
 */
public class ChatProvider
    extends InvocationProvider implements ChatCodes
{
    /**
     * Processes a request from a client to deliver a tell message to
     * another client.
     */
    public void handleTellRequest (
        BodyObject source, int invid, String target, String message)
    {
        // look up the target body object
        BodyObject tobj = PartyServer.lookupBody(target);
        if (tobj == null) {
            sendResponse(source, invid, TELL_FAILED_RESPONSE,
                         USER_NOT_ONLINE);
        }

        // deliver a tell notification to the target player
        Object[] args = new Object[] { source.username, message };
        PartyServer.invmgr.sendNotification(
            tobj.getOid(), MODULE_NAME, TELL_NOTIFICATION, args);

        sendResponse(source, invid, TELL_SUCCEEDED_RESPONSE);
    }
}
