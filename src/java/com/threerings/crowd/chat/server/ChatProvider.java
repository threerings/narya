//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.server;

import java.util.Iterator;

import com.samskivert.util.StringUtil;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.AccessControl;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.chat.client.ChatService.TellListener;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * The chat provider handles the server side of the chat-related
 * invocation services.
 */
public class ChatProvider
    implements ChatCodes, InvocationProvider
{
    /** The access control identifier for broadcast chat privileges. */
    public static final String BROADCAST_TOKEN = "crowd.chat.broadcast";

    /** Interface to allow an auto response to a tell message. */
    public static interface TellAutoResponder
    {
        /**
         * Called following the delivery of <code>message</code> from
         * <code>teller</code> to <code>tellee</code>.
         */
        public void sentTell (BodyObject teller, BodyObject tellee,
                              String message);
    }

    /**
     * Set the auto tell responder for the chat provider. Only one auto
     * responder is allowed.
     */
    public static void setTellAutoResponder (TellAutoResponder autoRespond)
    {
        _autoRespond = autoRespond;
    }

    /**
     * Set the authorizer we will use to see if the user is allowed to
     * perform various chatting actions.
     */
    public static void setCommunicationAuthorizer (
        CommunicationAuthorizer comAuth)
    {
        _comAuth = comAuth;
    }

    /**
     * Set an object to which all broadcasts should be sent, rather
     * than iterating over the place objects and sending to each of them.
     *
     * @param object an object to send all broadcasts, or null to send to
     * each place object instead.
     */
    public static void setAlternateBroadcastObject (DObject object)
    {
        _broadcastObject = object;
    }

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
    public void tell (ClientObject caller, Name target, String message,
                      TellListener listener)
        throws InvocationException
    {
        // make sure the caller is authorized to perform this action
        if ((_comAuth != null) && (!_comAuth.authorized(caller))) {
            return;
        }

        // make sure the target user is online
        BodyObject tobj = CrowdServer.lookupBody(target);
        if (tobj == null) {
            throw new InvocationException(USER_NOT_ONLINE);
        }

        if (tobj.status == OccupantInfo.DISCONNECTED) {
            throw new InvocationException(MessageBundle.compose(
                USER_DISCONNECTED, TimeUtil.getTimeOrderString(
                System.currentTimeMillis() - tobj.statusTime,
                TimeUtil.SECOND)));
        }

        // deliver a tell notification to the target player
        BodyObject source = (BodyObject)caller;
        sendTellMessage(tobj, source.username, null, message);

        // let the teller know it went ok
        long idle = 0L;
        if (tobj.status == OccupantInfo.IDLE) {
            idle = System.currentTimeMillis() - tobj.statusTime;
        }
        String awayMessage = null;
        if (!StringUtil.blank(tobj.awayMessage)) {
            awayMessage = tobj.awayMessage;
        }
        listener.tellSucceeded(idle, awayMessage);

        // do the autoresponse if needed
        if (_autoRespond != null) {
            _autoRespond.sentTell(source, tobj, message);
        }
    }

    /**
     * Processes a {@link ClientService#broadcast} request.
     */
    public void broadcast (ClientObject caller, String message,
                           InvocationListener listener)
        throws InvocationException
    {
        BodyObject body = (BodyObject)caller;

        // make sure the requesting user has broadcast privileges
        if (!CrowdServer.actrl.checkAccess(body, BROADCAST_TOKEN)) {
            throw new InvocationException(AccessControl.LACK_ACCESS);
        }

        broadcast(body.username, null, message, false);
    }

    /**
     * Broadcast the specified message to all places in the game.
     *
     * @param from the user the broadcast is from, or null to send the message
     * as a system message.
     * @param bundle the bundle, or null if the message needs no translation.
     * @param msg the content of the message to broadcast.
     * @param attention if true, the message is sent as ATTENTION level,
     * otherwise as INFO. Ignored if from is non-null.
     */
    public static void broadcast (Name from, String bundle, String msg,
                                  boolean attention)
    {
        if (_broadcastObject != null) {
            broadcastTo(_broadcastObject, from, bundle, msg, attention);

        } else {
            Iterator iter = CrowdServer.plreg.enumeratePlaces();
            while (iter.hasNext()) {
                PlaceObject plobj = (PlaceObject)iter.next();
                if (plobj.shouldBroadcast()) {
                    broadcastTo(plobj, from, bundle, msg, attention);
                }
            }
        }
    }

    /**
     * Direct a broadcast to the specified object.
     */
    protected static void broadcastTo (DObject object,
        Name from, String bundle, String msg, boolean attention)
    {
        if (from == null) {
            if (attention) {
                SpeakProvider.sendAttention(object, bundle, msg);
            } else {
                SpeakProvider.sendInfo(object, bundle, msg);
            }

        } else {
            SpeakProvider.sendSpeak(object, from, bundle, msg,
                                    BROADCAST_MODE);
        }
    }

    /**
     * Processes a {@link ClientService#away} request.
     */
    public void away (ClientObject caller, String message)
    {
        BodyObject body = (BodyObject)caller;
        // we modify this field via an invocation service request because
        // a body object is not modifiable by the client
        body.setAwayMessage(message);
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
        BodyObject target, Name speaker, String bundle, String message)
    {
        UserMessage msg =
            new UserMessage(message, bundle, speaker, DEFAULT_MODE);
        SpeakProvider.sendMessage(target, msg);

        // note that the teller "heard" what they said
        SpeakProvider.noteMessage(speaker, msg);
    }

    /** The distributed object manager used by the chat services. */
    protected static DObjectManager _omgr;

    /** Reference to our auto responder object. */
    protected static TellAutoResponder _autoRespond;

    /** The entity that will authorize our chatters. */
    protected static CommunicationAuthorizer _comAuth;

    /** An alternative object to which broadcasts should be sent. */
    protected static DObject _broadcastObject;
}
