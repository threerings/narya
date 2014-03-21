//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.client.ChatService.TellListener;
import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMarshaller;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.BodyLocal;
import com.threerings.crowd.server.BodyLocator;
import com.threerings.crowd.server.PlaceRegistry;

/**
 * The chat provider handles the server side of the chat-related invocation services.
 */
@Singleton
public class ChatProvider
    implements InvocationProvider
{
    /** Interface to allow an auto response to a tell message. */
    public static interface TellAutoResponder
    {
        /**
         * Called following the delivery of <code>message</code> from <code>teller</code> to
         * <code>tellee</code>.
         */
        void sentTell (BodyObject teller, BodyObject tellee, String message);
    }

    /** Used to forward certain types of chat messages between servers in a multi-server setup. */
    public static interface ChatForwarder
    {
        /**
         * Requests that the supplied tell message be delivered to the appropriate destination.
         *
         * @return true if the tell was delivered, false otherwise.
         */
        boolean forwardTell (UserMessage message, Name target, TellListener listener);

        /**
         * Requests that the supplied broadcast message be delivered on other servers.
         */
        void forwardBroadcast (Name from, byte levelOrMode, String bundle, String msg);
    }

    /**
     * Creates and registers this chat provider.
     */
    @Inject public ChatProvider (InvocationManager invmgr)
    {
        // register a chat provider with the invocation manager
        invmgr.registerProvider(this, ChatMarshaller.class, CrowdCodes.CROWD_GROUP);
    }

    /**
     * Set an object to which all broadcasts should be sent, rather than iterating over the place
     * objects and sending to each of them.
     *
     * @param object an object to send all broadcasts, or null to send to each place object
     * instead.
     */
    public void setAlternateBroadcastObject (DObject object)
    {
        _broadcastObject = object;
    }

    /**
     * Set the auto tell responder for the chat provider. Only one auto responder is allowed.
     * <em>Note:</em> this only works for same-server tells. If the tell is forwarded to another
     * server, no auto-response opportunity is provided (because we never have both body objects in
     * the same place).
     */
    public void setTellAutoResponder (TellAutoResponder autoRespond)
    {
        _autoRespond = autoRespond;
    }

    /**
     * Configures the chat forwarder. This is used by the Crowd peer services to forward messages
     * between servers in a multi-server cluster.
     */
    public void setChatForwarder (ChatForwarder forwarder)
    {
        _chatForwarder = forwarder;
    }

    /**
     * Processes a {@link ChatService#tell} request.
     */
    public void tell (ClientObject caller, Name target, String message, TellListener listener)
        throws InvocationException
    {
        BodyObject source = _locator.forClient(caller);

        // ensure that the caller has normal chat privileges
        InvocationException.requireAccess(source, ChatCodes.CHAT_ACCESS);

        // deliver the tell message to the target
        deliverTell(createTellMessage(source, message), target, listener);

        // inform the auto-responder if needed
        BodyObject targobj;
        if (_autoRespond != null && (targobj = _locator.lookupBody(target)) != null) {
            _autoRespond.sentTell(source, targobj, message);
        }
    }

    /**
     * Processes a {@link ChatService#broadcast} request.
     */
    public void broadcast (ClientObject caller, String message, InvocationListener listener)
        throws InvocationException
    {
        BodyObject body = _locator.forClient(caller);

        // make sure the requesting user has broadcast privileges
        InvocationException.requireAccess(body, ChatCodes.BROADCAST_ACCESS);
        broadcast(body.getVisibleName(), null, message, false, true);
    }

    /**
     * Processes a {@link ChatService#away} request.
     */
    public void away (ClientObject caller, String message)
    {
        BodyObject body = _locator.forClient(caller);
        // we modify this field via an invocation service request because a body object is not
        // modifiable by the client
        body.setAwayMessage(message);
    }

    /**
     * Broadcasts the specified message to all place objects in the system.
     *
     * @param from the user the broadcast is from, or null to send the message as a system message.
     * @param bundle the bundle, or null if the message needs no translation.
     * @param msg the content of the message to broadcast.
     * @param attention if true, the message is sent as ATTENTION level, otherwise as INFO. Ignored
     * if from is non-null.
     * @param forward if true, forward this broadcast on to any registered chat forwarder, if
     * false, deliver it only locally on this server.
     */
    public void broadcast (Name from, String bundle, String msg, boolean attention, boolean forward)
    {
        byte levelOrMode = (from != null) ? ChatCodes.BROADCAST_MODE
            : (attention ? SystemMessage.ATTENTION : SystemMessage.INFO);
        broadcast(from, levelOrMode, bundle, msg, forward);
    }

    /**
     * Broadcast with support for a customizable level or mode.
     * @param levelOrMode if from is null, it's an attentionLevel, else it's a mode code.
     */
    public void broadcast (Name from, byte levelOrMode, String bundle, String msg, boolean forward)
    {
        if (_broadcastObject != null) {
            broadcastTo(_broadcastObject, from, levelOrMode, bundle, msg);

        } else {
            for (Iterator<PlaceObject> iter = _plreg.enumeratePlaces(); iter.hasNext(); ) {
                PlaceObject plobj = iter.next();
                if (plobj.shouldBroadcast()) {
                    broadcastTo(plobj, from, levelOrMode, bundle, msg);
                }
            }
        }

        if (forward && _chatForwarder != null) {
            _chatForwarder.forwardBroadcast(from, levelOrMode, bundle, msg);
        }
    }

    /**
     * Delivers a tell message to the specified target and notifies the supplied listener of the
     * result. It is assumed that the teller has already been permissions checked.
     */
    public void deliverTell (UserMessage message, Name target, TellListener listener)
        throws InvocationException
    {
        // make sure the target user is online
        BodyObject tobj = _locator.lookupBody(target);
        if (tobj == null) {
            // if we have a forwarder configured, try forwarding the tell
            if (_chatForwarder != null && _chatForwarder.forwardTell(message, target, listener)) {
                return;
            }
            throw new InvocationException(ChatCodes.USER_NOT_ONLINE);
        }

        if (tobj.status == OccupantInfo.DISCONNECTED) {
            String errmsg = MessageBundle.compose(
                ChatCodes.USER_DISCONNECTED, TimeUtil.getTimeOrderString(
                    System.currentTimeMillis() - tobj.getLocal(BodyLocal.class).statusTime,
                    TimeUtil.SECOND));
            throw new InvocationException(errmsg);
        }

        // deliver a tell notification to the target player
        deliverTell(tobj, message);

        // let the teller know it went ok
        long idle = 0L;
        if (tobj.status == OccupantInfo.IDLE) {
            idle = System.currentTimeMillis() - tobj.getLocal(BodyLocal.class).statusTime;
        }
        String awayMessage = null;
        if (!StringUtil.isBlank(tobj.awayMessage)) {
            awayMessage = tobj.awayMessage;
        }
        listener.tellSucceeded(idle, awayMessage);
    }

    /**
     * Delivers a tell notification to the specified target player. It is assumed that the message
     * is coming from some server entity and need not be permissions checked or notified of the
     * result.
     */
    public void deliverTell (BodyObject target, UserMessage message)
    {
        SpeakUtil.sendMessage(target, message);

        // note that the teller "heard" what they said
        SpeakUtil.noteMessage(target, message.speaker, message);
    }

    /**
     * Used to create a {@link UserMessage} for the supplied sender.
     */
    protected UserMessage createTellMessage (BodyObject source, String message)
    {
        return UserMessage.create(source.getVisibleName(), message);
    }

    /**
     * Direct a broadcast to the specified object.
     */
    protected void broadcastTo (
        DObject object, Name from, byte levelOrMode, String bundle, String msg)
    {
        if (from == null) {
            SpeakUtil.sendSystem(object, bundle, msg, levelOrMode /* level */);

        } else {
            SpeakUtil.sendSpeak(object, from, bundle, msg, levelOrMode /* mode */);
        }
    }

    /** Provides access to place managers. */
    @Inject protected PlaceRegistry _plreg;

    /** Used to look up body objects by name. */
    @Inject protected BodyLocator _locator;

    /** Generates auto-responses to tells. May be null. */
    protected TellAutoResponder _autoRespond;

    /** Forwards chat between servers. May be null. */
    protected ChatForwarder _chatForwarder;

    /** An alternative object to which broadcasts should be sent. */
    protected DObject _broadcastObject;
}
