//
// $Id: ChatProvider.java,v 1.8 2001/12/17 00:52:43 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.Log;

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

        } else {
            // deliver a tell notification to the target player
            Object[] args = new Object[] { source.username, message };
            CrowdServer.invmgr.sendNotification(
                tobj.getOid(), MODULE_NAME, TELL_NOTIFICATION, args);
            // let the teller know it went ok
            sendResponse(source, invid, TELL_SUCCEEDED_RESPONSE);
        }
    }

    /**
     * Sends a chat notification to the specified place object originating
     * with the specified speaker and with the supplied message content.
     *
     * @param placeOid the place to which to deliver the chat message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param message the text of the chat message.
     */
    public static void sendChatMessage (
        int placeOid, String speaker, String message)
    {
        Object[] outargs = new Object[] { speaker, message };
        MessageEvent nevt = new MessageEvent(
            placeOid, ChatService.SPEAK_NOTIFICATION, outargs);
        CrowdServer.omgr.postEvent(nevt);
    }

    /**
     * Sends a system message notification to the specified place object
     * with the supplied message content. A system message is one that
     * will be rendered where the chat messages are rendered, but in a way
     * that makes it clear that it is a message from the server.
     *
     * @param placeOid the place to which to deliver the message.
     * @param message the text of the message.
     */
    public static void sendSystemMessage (int placeOid, String message)
    {
        Object[] outargs = new Object[] { message };
        MessageEvent nevt = new MessageEvent(
            placeOid, ChatService.SYSTEM_NOTIFICATION, outargs);
        CrowdServer.omgr.postEvent(nevt);
    }
}
