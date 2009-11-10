//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.peer.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.internal.Lists;

import com.samskivert.util.Comparators;
import com.samskivert.util.Interval;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.ResultListener;
import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.PeerManager;
import com.threerings.presents.peer.server.PeerNode;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.server.ChatProvider;
import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.crowd.chat.server.SpeakUtil.ChatHistoryEntry;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.peer.data.CrowdClientInfo;
import com.threerings.crowd.peer.data.CrowdNodeObject;

import static com.threerings.crowd.Log.log;

/**
 * Extends the standard peer manager and bridges certain Crowd services.
 */
public abstract class CrowdPeerManager extends PeerManager
    implements CrowdPeerProvider, ChatProvider.ChatForwarder
{
    /**
     * Value asynchronously returned by {@link #collectChatHistory} after polling all peer nodes.
     */
    public static class ChatHistoryResult
    {
        /** The set of nodes that either did not reply within the timeout, or had a failure. */
        public Set<String> failedNodes;

        /** The things in the user's chat history, aggregated from all nodes and sorted by
         * timestamp. */
        public List<ChatHistoryEntry> history;
    }

    /**
     * Creates an uninitialized peer manager.
     */
    @Inject public CrowdPeerManager (Lifecycle cycle)
    {
        super(cycle);
    }

    // from interface CrowdPeerProvider
    public void deliverTell (ClientObject caller, UserMessage message,
                             Name target, ChatService.TellListener listener)
        throws InvocationException
    {
        // we just forward the message as if it originated on this server
        _chatprov.deliverTell(message, target, listener);
    }

    // from interface CrowdPeerProvider
    public void deliverBroadcast (
        ClientObject caller, Name from, byte levelOrMode, String bundle, String msg)
    {
        // deliver the broadcast locally on this server
        _chatprov.broadcast(from, levelOrMode, bundle, msg, false);
    }

    // from interface CrowdPeerProvider
    public void getChatHistory (
        ClientObject caller, Name user, InvocationService.ResultListener lner)
        throws InvocationException
    {
        lner.requestProcessed(Lists.newArrayList(Iterables.filter(
            SpeakUtil.getChatHistory(user), IS_USER_MESSAGE)));
    }

    // from interface ChatProvider.ChatForwarder
    public boolean forwardTell (UserMessage message, Name target,
                                ChatService.TellListener listener)
    {
        // look up their auth username from their visible name
        Name username = authFromViz(target);
        if (username == null) {
            return false; // sorry kid, don't know ya
        }

        // look through our peers to see if the target user is online on one of them
        for (PeerNode peer : _peers.values()) {
            CrowdNodeObject cnobj = (CrowdNodeObject)peer.nodeobj;
            if (cnobj == null) {
                continue;
            }
            // we have to use auth username to look up their ClientInfo
            CrowdClientInfo cinfo = (CrowdClientInfo)cnobj.clients.get(username);
            if (cinfo != null) {
                cnobj.crowdPeerService.deliverTell(peer.getClient(), message, target, listener);
                return true;
            }
        }
        return false;
    }

    // from interface ChatProvider.ChatForwarder
    public void forwardBroadcast (Name from, byte levelOrMode, String bundle, String msg)
    {
        for (PeerNode peer : _peers.values()) {
            if (peer.nodeobj != null) {
                ((CrowdNodeObject)peer.nodeobj).crowdPeerService.deliverBroadcast(
                    peer.getClient(), from, levelOrMode, bundle, msg);
            }
        }
    }

    /**
     * Collects all chat messages heard by the given user on all peers. Must be called on the
     * dobj event thread.
     */
    public void collectChatHistory (Name user, ResultListener<ChatHistoryResult> lner)
    {
        _omgr.requireEventThread();
        new ChatHistoryCollector(user, lner).collect();
    }

    @Override // from PeerManager
    public void shutdown ()
    {
        super.shutdown();

        // unregister our invocation service
        if (_nodeobj != null) {
            _invmgr.clearDispatcher(((CrowdNodeObject)_nodeobj).crowdPeerService);
        }

        // clear our chat forwarder registration
        _chatprov.setChatForwarder(null);
    }

    @Override // from PeerManager
    protected NodeObject createNodeObject ()
    {
        return new CrowdNodeObject();
    }

    @Override // from PeerManager
    protected ClientInfo createClientInfo ()
    {
        return new CrowdClientInfo();
    }

    @Override // from PeerManager
    protected void initClientInfo (PresentsSession client, ClientInfo info)
    {
        super.initClientInfo(client, info);
        ((CrowdClientInfo)info).visibleName =
            ((BodyObject)client.getClientObject()).getVisibleName();
    }

    @Override // from PeerManager
    protected void didInit ()
    {
        super.didInit();

        // register and initialize our invocation service
        CrowdNodeObject cnobj = (CrowdNodeObject)_nodeobj;
        cnobj.setCrowdPeerService(_invmgr.registerDispatcher(new CrowdPeerDispatcher(this)));

        // register ourselves as a chat forwarder
        _chatprov.setChatForwarder(this);
    }

    /**
     * Converts a visible name to an authentication name. If this method returns null, the chat
     * system will act as if the vizname in question is not online.
     */
    protected abstract Name authFromViz (Name vizname);

    /**
     * Asynchronously collects the chat history from all nodes for a given user.
     * TODO: refactor node parts into base class similar to PeerManager.NodeAction
     */
    protected class ChatHistoryCollector
    {
        public ChatHistoryCollector (Name user, ResultListener<ChatHistoryResult> listener)
        {
            _user = user;
            _listener = listener;

            _result = new ChatHistoryResult();
            _result.failedNodes = Sets.newHashSet();
            _waiting = Sets.newHashSet();
        }

        public void collect ()
        {
            _result.history = Lists.newArrayList(
                Iterables.filter(SpeakUtil.getChatHistory(_user), IS_USER_MESSAGE));

            for (PeerNode peer : _peers.values()) {
                final PeerNode fpeer = peer;
                ((CrowdNodeObject)peer.nodeobj).crowdPeerService.getChatHistory(
                    peer.getClient(), _user, new InvocationService.ResultListener() {
                        @Override public void requestProcessed (Object result) {
                            processed(fpeer, result);
                        }
                        @Override public void requestFailed (String cause) {
                            failed(fpeer, cause);
                        }
                    });
                _waiting.add(peer.getNodeName());
            }

            // timeout in 5 seconds if we haven't heard back from all nodes
            final long TIMEOUT = 5 * 1000;
            if (!maybeComplete()) {
                new Interval(_omgr) {
                    @Override public void expired () {
                        checkTimeout();
                    }
                }.schedule(TIMEOUT);
            }
        }

        protected void processed (PeerNode node, Object result)
        {
            String name = node.getNodeName();
            if (!_waiting.remove(name)) {
                log.warning("Double chat history response from node", "name", name);
                return;
            }

            @SuppressWarnings("unchecked")
            List<ChatHistoryEntry> nodeMessages = (List<ChatHistoryEntry>)result;
            _result.history.addAll(nodeMessages);
            maybeComplete();
        }

        protected void failed (PeerNode node, String cause)
        {
            String name = node.getNodeName();
            _result.failedNodes.add(name);

            if (_waiting.remove(name)) {
                maybeComplete();
            } else {
                log.warning("Double chat history response from node", "name", name);
            }
        }

        protected boolean maybeComplete ()
        {
            if (!_waiting.isEmpty()) {
                return false;
            }

            Collections.sort(_result.history, SORT_BY_TIMESTAMP);
            _listener.requestCompleted(_result);
            return true;
        }

        protected void checkTimeout ()
        {
            if (!_waiting.isEmpty()) {
                _result.failedNodes.addAll(_waiting);
                _waiting.clear();
                maybeComplete();
            }
        }

        protected Name _user;
        protected ResultListener<ChatHistoryResult> _listener;
        protected ChatHistoryResult _result;
        protected Set<String> _waiting;
    }

    @Inject protected InvocationManager _invmgr;
    @Inject protected ChatProvider _chatprov;

    protected static final Predicate<ChatHistoryEntry> IS_USER_MESSAGE =
        new Predicate<ChatHistoryEntry>() {
        @Override public boolean apply (ChatHistoryEntry entry) {
            return entry.message instanceof UserMessage;
        }
    };

    protected static final Comparator<ChatHistoryEntry> SORT_BY_TIMESTAMP =
        new Comparator<ChatHistoryEntry>() {
        @Override public int compare (ChatHistoryEntry e1, ChatHistoryEntry e2) {
            return Comparators.compare(e1.message.timestamp, e2.message.timestamp);
        }
    };
}
