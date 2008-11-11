//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.IntSet;
import com.samskivert.util.Interval;

import com.threerings.util.Name;

import com.threerings.presents.annotation.AnyThread;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.PeerManager;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.server.ShutdownManager;

import com.threerings.crowd.chat.data.ChatChannel;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.peer.data.CrowdClientInfo;
import com.threerings.crowd.peer.data.CrowdNodeObject;
import com.threerings.crowd.peer.server.CrowdPeerManager;

import static com.threerings.crowd.Log.log;

/**
 * Handles chat channel services.
 */
@Singleton
public abstract class ChatChannelManager
    implements ChannelSpeakProvider, ShutdownManager.Shutdowner
{
    /**
     * When a body becomes a member of a channel, this method should be called so that any server
     * that happens to be hosting that channel can be told that the body in question is now a
     * participant.
     */
    @AnyThread
    public void bodyAddedToChannel (ChatChannel channel, int bodyId)
    {
        _peerMan.invokeNodeAction(new ParticipantChanged(channel, bodyId, true));
    }

    /**
     * When a body loses channel membership, this method should be called so that any server that
     * happens to be hosting that channel can be told that the body in question is now a
     * participant.
     */
    @AnyThread
    public void bodyRemovedFromChannel (ChatChannel channel, int bodyId)
    {
        _peerMan.invokeNodeAction(new ParticipantChanged(channel, bodyId, false));
    }

    // from interface ChannelSpeakProvider
    public void speak (ClientObject caller, final ChatChannel channel, String message, byte mode)
    {
        final UserMessage umsg = new UserMessage(
            ((BodyObject)caller).getVisibleName(), null, message, mode);

        // if we're hosting this channel, dispatch it directly
        if (_channels.containsKey(channel)) {
            dispatchSpeak(channel, umsg);
            return;
        }

        // if we're resolving this channel, queue up our message for momentary deliver
        List<UserMessage> msgs = _resolving.get(channel);
        if (msgs != null) {
            msgs.add(umsg);
            return;
        }

        // forward the speak request to the server that hosts the channel in question
        _peerMan.invokeNodeAction(new ForwardChannelSpeak(channel, umsg), new Runnable() {
            public void run () {
                _resolving.put(channel, Lists.newArrayList(umsg));
                resolveAndDispatch(channel);
            }
        });
    }

    // from interface ShutdownManager.Shutdowner
    public void shutdown ()
    {
        // stop our channel closer; always be closing... except now
        _closer.cancel();
        _closer = null;
    }

    /**
     * Creates our singleton manager and registers our invocation service.
     */
    @Inject protected ChatChannelManager (PresentsDObjectMgr omgr, InvocationManager invmgr,
                                          ShutdownManager shutmgr)
    {
        invmgr.registerDispatcher(new ChannelSpeakDispatcher(this), CrowdCodes.CROWD_GROUP);
        shutmgr.registerShutdowner(this);

        // create and start our idle channel closer (always be closing)
        _closer = new Interval(omgr) {
            @Override
            public void expired () {
                closeIdleChannels();
            }
        };
        _closer.schedule(IDLE_CHANNEL_CHECK_PERIOD, true);
    }

    /**
     * Resolves the channel specified in the supplied action and then dispatches it.
     */
    protected void resolveAndDispatch (final ChatChannel channel)
    {
        NodeObject.Lock lock = new NodeObject.Lock("ChatChannel", channel.getLockName());
        _peerMan.performWithLock(lock, new PeerManager.LockedOperation() {
            public void run () {
                ((CrowdNodeObject)_peerMan.getNodeObject()).addToHostedChannels(channel);
                finishResolveAndDispatch(channel);
            }
            public void fail (String peerName) {
                List<UserMessage> msgs = _resolving.remove(channel);
                if (peerName == null) {
                    log.warning("Failed to resolve chat channel due to lock failure",
                                "channel", channel);
                } else {
                    // some other peer resolved this channel first, so forward any queued messages
                    // directly to that node
                    for (UserMessage msg : msgs) {
                        _peerMan.invokeNodeAction(peerName, new ForwardChannelSpeak(channel, msg));
                    }
                }
            }
        });
    }

    /**
     * Resolves the participant set for the specified chat channel and dispatches all pending
     * messages to the channel. End users of the chat channel system should override this method
     * and do what is necessary to resolve the channel's participant set and call {@link
     * #resolutionComplete} or {@link #resolutionFailed}.
     */
    protected void finishResolveAndDispatch (ChatChannel channel)
    {
        resolutionComplete(channel, new ArrayIntSet());
    }

    /**
     * This should be called when a channel's participant set has been resolved.
     */
    protected void resolutionComplete (ChatChannel channel, IntSet parts)
    {
        // map the participants of our now resolved channel
        ChannelInfo info = new ChannelInfo();
        info.participants = parts;
        _channels.put(channel, info);

        // dispatch any pending messages now that we know where they go
        for (UserMessage msg : _resolving.remove(channel)) {
            dispatchSpeak(channel, msg);
        }
    }

    /**
     * This should be called if channel resolution fails.
     */
    protected void resolutionFailed (ChatChannel channel, Exception cause)
    {
        log.warning("Failed to resolve chat channel", "channel", channel, cause);

        // alas, we just drop all pending messages because we're hosed
        _resolving.remove(channel);
    }

    /**
     * Requests that we dispatch the supplied message to all participants of the specified chat
     * channel. The speaker will be validated prior to dispatching the message as the originating
     * server does not have the information it needs to validate the speaker and must leave that to
     * us, the channel hosting server.
     */
    protected void dispatchSpeak (ChatChannel channel, UserMessage message)
    {
        final ChannelInfo info = _channels.get(channel);
        if (info == null) {
            // TODO: maybe we should just reresolve the channel...
            log.warning("Requested to dispatch speak on unhosted channel", "channel", channel,
                        "msg", message);
            return;
        }

        // validate the speaker
        if (!info.participants.contains(getBodyId(message.speaker))) {
            log.warning("Dropping channel chat message from non-speaker", "channel", channel,
                        "message", message);
            return;
        }

        // note that we're dispatching a message on this channel
        info.lastMessage = System.currentTimeMillis();

        // generate a mapping from node name to an array of body ids for the participants that are
        // currently on the node in question
        final Map<String,int[]> partMap = Maps.newHashMap();
        _peerMan.applyToNodes(new Function<NodeObject,Void>() {
            public Void apply (NodeObject nodeobj) {
                ArrayIntSet nodeBodyIds = new ArrayIntSet();
                for (ClientInfo clinfo : nodeobj.clients) {
                    int bodyId = getBodyId(((CrowdClientInfo)clinfo).visibleName);
                    if (info.participants.contains(bodyId)) {
                        nodeBodyIds.add(bodyId);
                    }
                }
                partMap.put(nodeobj.nodeName, nodeBodyIds.toIntArray());
                return null;
            }
        });

        for (Map.Entry<String,int[]> entry : partMap.entrySet()) {
            _peerMan.invokeNodeAction(
                entry.getKey(), new DispatchChannelSpeak(channel, message, entry.getValue()));
        }
    }

    /**
     * Delivers the supplied chat channel message to the specified bodies.
     */
    protected void deliverSpeak (ChatChannel channel, UserMessage message, int[] bodyIds)
    {
        for (int bodyId : bodyIds) {
            BodyObject bobj = getBodyObject(bodyId);
            if (bobj != null && shouldDeliverSpeak(channel, message, bobj)) {
                bobj.postMessage(ChatCodes.CHAT_CHANNEL_NOTIFICATION, channel, message);
            }
        }
    }

    /**
     * Called periodically to check for and close any channels that have been idle too long.
     */
    protected void closeIdleChannels ()
    {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<ChatChannel, ChannelInfo>> iter = _channels.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ChatChannel, ChannelInfo> entry = iter.next();
            if (now - entry.getValue().lastMessage > IDLE_CHANNEL_CLOSE_TIME) {
                ((CrowdNodeObject)_peerMan.getNodeObject()).removeFromHostedChannels(entry.getKey());
                iter.remove();
            }
        }
    }

    /**
     * Ratifies the delivery of the supplied chat channel message to the specified body. Derived
     * classes can override this method to implement channel disabling, mute lists or any other
     * suppression they might need.
     */
    protected boolean shouldDeliverSpeak (ChatChannel channel, UserMessage message, BodyObject body)
    {
        return true;
    }

    /**
     * Converts a speaker's visible name into a unique integer id. This is not the oid for this
     * speaker but rather a persistent integer identifier that can be passed between servers and
     * used to look up the speaker on the target server via a call to {@link #getBodyObject}. We
     * use this rather than names to avoid having to send (large) {@link Name} objects for every
     * channel participant to each individual peer that will be forwarding messages.
     */
    protected abstract int getBodyId (Name speaker);

    /**
     * Locates a body object from the given unique id. May return null.
     */
    protected abstract BodyObject getBodyObject (int bodyId);

    /** Forwards a channel speak request from the server hosting the message originator to the
     * server that is hosting the channel. */
    protected abstract static class ChannelAction extends PeerManager.NodeAction
    {
        public ChannelAction (ChatChannel channel) {
            _channel = channel;
        }
        public ChannelAction () {
        }
        @Override public boolean isApplicable (NodeObject nodeobj) {
            return ((CrowdNodeObject)nodeobj).hostedChannels.contains(_channel);
        }
        protected ChatChannel _channel;
        @Inject protected transient ChatChannelManager _channelMan;
    }

    /** Informs the server hosting a channel that a body has been added to or removed from the
     * channel's participants set. */
    protected static class ParticipantChanged extends ChannelAction
    {
        public ParticipantChanged (ChatChannel channel, int bodyId, boolean added) {
            super(channel);
            _bodyId = bodyId;
            _added = added;
        }
        public ParticipantChanged () {
        }
        @Override protected void execute () {
            ChannelInfo info = _channelMan._channels.get(_channel);
            if (info != null) {
                if (_added) {
                    info.participants.add(_bodyId);
                } else {
                    info.participants.remove(_bodyId);
                }
            } else if (_channelMan._resolving.containsKey(_channel)) {
                log.warning("Oh for fuck's sake, distributed systems are complicated",
                            "channel", _channel);
            }
        }
        protected int _bodyId;
        protected boolean _added;
    }

    /** Forwards a channel speak request from the server hosting the message originator to the
     * server that is hosting the channel. */
    protected static class ForwardChannelSpeak extends ChannelAction
    {
        public ForwardChannelSpeak (ChatChannel channel, UserMessage message) {
            super(channel);
            _message = message;
        }
        public ForwardChannelSpeak () {
        }
        @Override protected void execute () {
            _channelMan.dispatchSpeak(_channel, _message);
        }
        protected UserMessage _message;
    }

    /** Forwards a chat channel message to the server to which some subset of the channel
     * participants are connected so that it can dispatch the message on their body objects. */
    protected static class DispatchChannelSpeak extends ForwardChannelSpeak
    {
        public DispatchChannelSpeak (ChatChannel channel, UserMessage message, int[] bodyIds) {
            super(channel, message);
            _bodyIds = bodyIds;
        }
        public DispatchChannelSpeak () {
        }
        @Override public boolean isApplicable (NodeObject nodeobj) {
            return true; // not used
        }
        @Override protected void execute () {
            _channelMan.deliverSpeak(_channel, _message, _bodyIds);
        }
        protected int[] _bodyIds;
    }

    /** Contains metadata for a particular channel. */
    protected static class ChannelInfo
    {
        /** The body ids of the participants of this channel. */
        public IntSet participants;

        /** The time at which a message was last dispatched on this channel. */
        public long lastMessage;
    }

    /** Used to close channels that have not had any activity in a few minutes. */
    protected Interval _closer;

    /** Contains pending messages for all channels currently being resolved. */
    protected Map<ChatChannel,List<UserMessage>> _resolving = Maps.newHashMap();

    /** A map of resolved channels to metadata records. */
    protected Map<ChatChannel,ChannelInfo> _channels = Maps.newHashMap();

    /** Provides peer services. */
    @Inject protected CrowdPeerManager _peerMan;

    /** The period on which we check for idle channels. */
    protected static final long IDLE_CHANNEL_CHECK_PERIOD = 5 * 1000L;

    /** The amount of idle time (in milliseconds) after which we close a channel. */
    protected static final long IDLE_CHANNEL_CLOSE_TIME = 5 * 60 * 1000L;
}
