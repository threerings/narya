//
// $Id: ChatProvider.java,v 1.2 2001/08/04 02:54:28 mdb Exp $

package com.threerings.cocktail.party.chat;

import com.threerings.cocktail.cher.server.InvocationProvider;
import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.server.PartyServer;

public class ChatProvider extends InvocationProvider
{
    /**
     * Processes a request from a client to deliver a tell message to
     * another client.
     */
    public Object[] handleTellRequest (
        BodyObject source, String target, String message)
    {
        // look up the target body object
        BodyObject tobj = PartyServer.lookupBody(target);
        if (tobj == null) {
            return createResponse("TellFailed", "m.player_not_online");
        }

        // deliver a tell notification to the target player
        Object[] args = new Object[] { source.username, message };
        PartyServer.invmgr.sendNotification(
            tobj.getOid(), ChatService.MODULE, ChatService.TELL_NOTIFICATION,
            args);

        return createResponse("TellSucceeded");
    }
}
