//
// $Id: ChatMessageHandler.java,v 1.2 2001/10/11 04:07:51 mdb Exp $

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
    public void handleEvent (MessageEvent event, PlaceObject target)
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
        Object[] outargs = new Object[] { source.username, message };
        MessageEvent nevt = new MessageEvent(
            target.getOid(), ChatService.SPEAK_NOTIFICATION, outargs);
        CrowdServer.omgr.postEvent(nevt);
    }
}
