//
// $Id: ChatProvider.java,v 1.12 2002/07/22 22:54:03 ray Exp $

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
            sendTellMessage(tobj.getOid(), source.username, null, message);
            // let the teller know it went ok
            sendResponse(source, invid, TELL_SUCCEEDED_RESPONSE);
        }
    }

    /**
     * Delivers a tell notification to the specified target player,
     * originating with the specified speaker.
     *
     * @param targetOid the body object id of the user that will receive
     * the tell message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle the bundle identifier that will be used by the client
     * to translate the message text (this would be null in all cases
     * except where the message originated from some server entity that
     * was "faking" a tell to a real player).
     * @param message the text of the chat message.
     */
    public static void sendTellMessage (
        int targetOid, String speaker, String bundle, String message)
    {
        Object[] args = null;
        if (bundle == null) {
            args = new Object[] { speaker, message };
        } else {
            args = new Object[] { speaker, bundle, message };
        }
        CrowdServer.invmgr.sendNotification(
            targetOid, MODULE_NAME, TELL_NOTIFICATION, args);
    }

    /**
     * Sends a chat notification to the specified place object originating
     * with the specified speaker (the speaker optionally being a server
     * entity that wishes to fake a "speak" message) and with the supplied
     * message content.
     *
     * @param placeOid the place to which to deliver the chat message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the chat message.
     */
    public static void sendChatMessage (
        int placeOid, String speaker, String bundle, String message)
    {
        sendChatMessage(
            placeOid, speaker, bundle, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Sends a chat notification to the specified place object originating
     * with the specified speaker (the speaker optionally being a server
     * entity that wishes to fake a "speak" message) and with the supplied
     * message content.
     *
     * @param placeOid the place to which to deliver the chat message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the chat message.
     * @param mode the mode of the message, @see ChatCodes.DEFAULT_MODE
     */
    public static void sendChatMessage (
        int placeOid, String speaker, String bundle, String message,
        byte mode)
    {
        Object[] outargs = null;
        if (bundle == null) {
            outargs = new Object[] { speaker, message, new Byte(mode) };
        } else {
            outargs = new Object[] { speaker, bundle, message, new Byte(mode) };
        }
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
     * @param bundle the name of the localization bundle that should be
     * used to translate this system message prior to displaying it to the
     * client.
     * @param message the text of the message.
     */
    public static void sendSystemMessage (
        int placeOid, String bundle, String message)
    {
        Object[] outargs = new Object[] { bundle, message };
        MessageEvent nevt = new MessageEvent(
            placeOid, ChatService.SYSTEM_NOTIFICATION, outargs);
        CrowdServer.omgr.postEvent(nevt);
    }
}
