//
// $Id: ChatProvider.java,v 1.5 2001/10/11 04:07:51 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.server.InvocationProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

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
        BodyObject tobj = CrowdServer.lookupBody(target);
        if (tobj == null) {
            sendResponse(source, invid, TELL_FAILED_RESPONSE,
                         USER_NOT_ONLINE);
        }

        // deliver a tell notification to the target player
        Object[] args = new Object[] { source.username, message };
        CrowdServer.invmgr.sendNotification(
            tobj.getOid(), MODULE_NAME, TELL_NOTIFICATION, args);

        sendResponse(source, invid, TELL_SUCCEEDED_RESPONSE);
    }
}
