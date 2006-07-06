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
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.chat.client.ChatService.TellListener;
import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * The chat provider handles the server side of the chat-related
 * invocation services.
 */
public class ChatProvider
    implements ChatCodes, InvocationProvider
{
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

    /** Used to forward tells between servers in a multi-server setup. */
    public static interface TellForwarder
    {
        /**
         * Requests that the supplied tell message be delivered to the
         * appropriate destination.
         *
         * @return true if the tell was delivered, false otherwise.
         */
        public boolean forwardTell (UserMessage message, Name target,
                                    TellListener listener);
    }

    /**
     * Set an object to which all broadcasts should be sent, rather
     * than iterating over the place objects and sending to each of them.
     *
     * @param object an object to send all broadcasts, or null to send to
     * each place object instead.
     */
    public void setAlternateBroadcastObject (DObject object)
    {
        _broadcastObject = object;
    }

    /**
     * Set the auto tell responder for the chat provider. Only one auto
     * responder is allowed. <em>Note:</em> this only works for same-server
     * tells. If the tell is forwarded to another server, no auto-response
     * opportunity is provided (because we never have both body objects in the
     * same place).
     */
    public void setTellAutoResponder (TellAutoResponder autoRespond)
    {
        _autoRespond = autoRespond;
    }

    /**
     * Configures the tell forwarded for the chat provider. This is used by the
     * Crowd peer services to forward tells between servers in a multi-server
     * cluster.
     */
    public void setTellForwarder (TellForwarder forwarder)
    {
        _tellForwarder = forwarder;
    }

    /**
     * Initializes the chat services and registers a chat provider with
     * the invocation manager.
     */
    public void init (InvocationManager invmgr, DObjectManager omgr)
    {
        // register a chat provider with the invocation manager
        invmgr.registerDispatcher(new ChatDispatcher(this), true);
    }

    /**
     * Processes a request from a client to deliver a tell message to
     * another client.
     */
    public void tell (ClientObject caller, Name target, String message,
                      TellListener listener)
        throws InvocationException
    {
        // ensure that the caller has normal chat privileges
        BodyObject source = (BodyObject)caller;
        String errmsg = source.checkAccess(CHAT_ACCESS, null);
        if (errmsg != null) {
            throw new InvocationException(errmsg);
        }

        // deliver the tell message to the target
        deliverTell(createTellMessage(source, message), target, listener);

        // inform the auto-responder if needed
        BodyObject targobj;
        if (_autoRespond != null &&
            (targobj = CrowdServer.lookupBody(target)) != null) {
            _autoRespond.sentTell(source, targobj, message);
        }
    }

    /**
     * Processes a {@link ChatService#broadcast} request.
     */
    public void broadcast (ClientObject caller, String message,
                           InvocationListener listener)
        throws InvocationException
    {
        // make sure the requesting user has broadcast privileges
        BodyObject body = (BodyObject)caller;
        String errmsg = body.checkAccess(BROADCAST_ACCESS, null);
        if (errmsg != null) {
            throw new InvocationException(errmsg);
        }
        broadcast(body.getVisibleName(), null, message, false);
    }

    /**
     * Processes a {@link ChatService#away} request.
     */
    public void away (ClientObject caller, String message)
    {
        BodyObject body = (BodyObject)caller;
        // we modify this field via an invocation service request because
        // a body object is not modifiable by the client
        body.setAwayMessage(message);
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
    public void broadcast (Name from, String bundle, String msg,
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
     * Delivers a tell message to the specified target and notifies the
     * supplied listener of the result. It is assumed that the teller has
     * already been permissions checked.
     */
    public void deliverTell (UserMessage message, Name target,
                             TellListener listener)
        throws InvocationException
    {
        // make sure the target user is online
        BodyObject tobj = CrowdServer.lookupBody(target);
        if (tobj == null) {
            // if we have a forwarder configured, try forwarding the tell
            if (_tellForwarder != null &&
                _tellForwarder.forwardTell(message, target, listener)) {
                return;
            }
            throw new InvocationException(USER_NOT_ONLINE);
        }

        if (tobj.status == OccupantInfo.DISCONNECTED) {
            String errmsg = MessageBundle.compose(
                USER_DISCONNECTED, TimeUtil.getTimeOrderString(
                    System.currentTimeMillis() - tobj.statusTime,
                    TimeUtil.SECOND));
            throw new InvocationException(errmsg);
        }

        // deliver a tell notification to the target player
        deliverTell(tobj, message);

        // let the teller know it went ok
        long idle = 0L;
        if (tobj.status == OccupantInfo.IDLE) {
            idle = System.currentTimeMillis() - tobj.statusTime;
        }
        String awayMessage = null;
        if (!StringUtil.isBlank(tobj.awayMessage)) {
            awayMessage = tobj.awayMessage;
        }
        listener.tellSucceeded(idle, awayMessage);
    }

    /**
     * Delivers a tell notification to the specified target player. It is
     * assumed that the message is coming from some server entity and need not
     * be permissions checked or notified of the result.
     */
    public void deliverTell (BodyObject target, UserMessage message)
    {
        SpeakProvider.sendMessage(target, message);

        // note that the teller "heard" what they said
        SpeakProvider.noteMessage(message.speaker, message);
    }

    /**
     * Used to create a {@link UserMessage} for the supplied sender.
     */
    protected UserMessage createTellMessage (BodyObject source, String message)
    {
        return new UserMessage(message, source.getVisibleName());
    }

    /**
     * Direct a broadcast to the specified object.
     */
    protected void broadcastTo (DObject object, Name from, String bundle,
                                String msg, boolean attention)
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

    /** Generates auto-responses to tells. May be null. */
    protected TellAutoResponder _autoRespond;

    /** Forwards tells between servers. May be null. */
    protected TellForwarder _tellForwarder;

    /** An alternative object to which broadcasts should be sent. */
    protected DObject _broadcastObject;
}
