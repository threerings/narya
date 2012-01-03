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

package com.threerings.crowd.peer.server;

import com.google.inject.Inject;

import com.samskivert.util.Lifecycle;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.PeerManager;
import com.threerings.presents.peer.server.PeerNode;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.server.ChatProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.peer.data.CrowdClientInfo;
import com.threerings.crowd.peer.data.CrowdNodeObject;
import com.threerings.crowd.peer.data.CrowdPeerMarshaller;
import com.threerings.crowd.server.BodyLocator;

/**
 * Extends the standard peer manager and bridges certain Crowd services.
 */
public abstract class CrowdPeerManager extends PeerManager
    implements CrowdPeerProvider, ChatProvider.ChatForwarder
{
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
                cnobj.crowdPeerService.deliverTell(message, target, listener);
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
                    from, levelOrMode, bundle, msg);
            }
        }
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
        BodyObject body = _locator.forClient(client.getClientObject());
        // body-less entities don't get a visibleName and so can't take part in chatting
        if (body != null) {
            ((CrowdClientInfo)info).visibleName = body.getVisibleName();
        }
    }

    @Override // from PeerManager
    protected void didInit ()
    {
        super.didInit();

        // register and initialize our invocation service
        CrowdNodeObject cnobj = (CrowdNodeObject)_nodeobj;
        cnobj.setCrowdPeerService(_invmgr.registerProvider(this, CrowdPeerMarshaller.class));

        // register ourselves as a chat forwarder
        _chatprov.setChatForwarder(this);
    }

    /**
     * Converts a visible name to an authentication name. If this method returns null, the chat
     * system will act as if the vizname in question is not online.
     */
    protected abstract Name authFromViz (Name vizname);

    @Inject protected ChatProvider _chatprov;
    @Inject protected BodyLocator _locator;
}
