//
// $Id: ChatProvider.java,v 1.13 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.chat.ChatService.TellListener;

/**
 * The chat provider handles the server side of the chat-related
 * invocation services.
 */
public class ChatProvider
    implements ChatCodes, InvocationProvider
{
    /**
     * Initializes the chat services and registers a chat provider with
     * the invocation manager.
     */
    public static void init (InvocationManager invmgr, DObjectManager omgr)
    {
        _omgr = omgr;

        // register a chat provider with the invocation manager
        invmgr.registerDispatcher(new ChatDispatcher(
                                      new ChatProvider()), true);
    }

    /**
     * Processes a request from a client to deliver a tell message to
     * another client.
     */
    public void tell (ClientObject caller, String target, String message,
                      TellListener listener)
        throws InvocationException
    {
        // make sure the target user is online
        BodyObject tobj = CrowdServer.lookupBody(target);
        if (tobj == null) {
            throw new InvocationException(USER_NOT_ONLINE);
        }

        // deliver a tell notification to the target player
        BodyObject source = (BodyObject)caller;
        sendTellMessage(tobj, source.username, null, message);

        // let the teller know it went ok
        listener.tellSucceeded();
    }

    /**
     * Delivers a tell notification to the specified target player,
     * originating with the specified speaker.
     *
     * @param target the body object of the user that will receive the
     * tell message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle the bundle identifier that will be used by the client
     * to translate the message text (this would be null in all cases
     * except where the message originated from some server entity that
     * was "faking" a tell to a real player).
     * @param message the text of the chat message.
     */
    public static void sendTellMessage (
        BodyObject target, String speaker, String bundle, String message)
    {
        ChatSender.sendTell(target, speaker, bundle, message);
    }

    /** The distributed object manager used by the chat services. */
    protected static DObjectManager _omgr;
}
