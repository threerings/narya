//
// $Id: ChatMessageHandler.java,v 1.6 2002/04/30 17:27:30 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;

/**
 * The chat message handler handles chat messages that are issued on a
 * place object with the intention of speaking to everyone in that place.
 */
public class ChatMessageHandler implements PlaceManager.MessageHandler
{
    /**
     * Handles {@link ChatCodes#SPEAK_REQUEST} messages.
     */
    public void handleEvent (MessageEvent event, PlaceManager pmgr)
    {
        // presently we do no ratification of chat messages, so we just
        // generate a chat notification with the message and name of the
        // speaker
        int soid = event.getSourceOid();
        BodyObject source = (BodyObject)CrowdServer.omgr.getObject(soid);
        if (source == null) {
            Log.info("Chatter went away. Dropping chat request " +
                     "[req=" + event + "].");
            return;
        }

        // parse our incoming arguments
        Object[] inargs = event.getArgs();
        int reqid = ((Integer)inargs[0]).intValue();
        String message = (String)inargs[1];

        // and generate a chat notification
        ChatProvider.sendChatMessage(
            event.getTargetOid(), source.username, null, message);
    }
}
