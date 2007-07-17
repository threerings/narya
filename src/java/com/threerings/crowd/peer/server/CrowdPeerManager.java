//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.util.Invoker;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsClient;

import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.PeerManager;
import com.threerings.presents.peer.server.PeerNode;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.server.ChatProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.peer.data.CrowdClientInfo;
import com.threerings.crowd.peer.data.CrowdNodeObject;
import com.threerings.crowd.peer.data.CrowdPeerMarshaller;

/**
 * Extends the standard peer manager and bridges certain Crowd services.
 */
public class CrowdPeerManager extends PeerManager
    implements CrowdPeerProvider, ChatProvider.TellForwarder
{
    /**
     * Creates a peer manager that integrates Crowd services across a cluster
     * of servers.
     */
    public CrowdPeerManager (ConnectionProvider conprov, Invoker invoker)
        throws PersistenceException
    {
        super(conprov, invoker);
    }

    // documentation inherited from interface CrowdPeerProvider
    public void deliverTell (ClientObject caller, UserMessage message,
                             Name target, ChatService.TellListener listener)
        throws InvocationException
    {
        // we just forward the message as if it originated on this server
        CrowdServer.chatprov.deliverTell(message, target, listener);
    }

    // documentation inherited from interface ChatProvider.TellForwarder
    public boolean forwardTell (UserMessage message, Name target,
                                ChatService.TellListener listener)
    {
        // look through our peer objects to see if the target user is online on
        // one of the other servers
        for (PeerNode peer : _peers.values()) {
            CrowdNodeObject cnobj = (CrowdNodeObject)peer.nodeobj;
            if (cnobj == null) {
                continue;
            }
            CrowdClientInfo cinfo = (CrowdClientInfo)cnobj.clients.get(target);
            if (cinfo != null) {
                cnobj.crowdPeerService.deliverTell(
                    peer.getClient(), message, target, listener);
                return true;
            }
        }
        return false;
    }

    @Override // documentation inherited
    public void shutdown ()
    {
        super.shutdown();

        // unregister our invocation service
        if (_nodeobj != null) {
            CrowdServer.invmgr.clearDispatcher(((CrowdNodeObject)_nodeobj).crowdPeerService);
        }

        // clear our tell forwarder registration
        CrowdServer.chatprov.setTellForwarder(null);
    }

    @Override // documentation inherited
    protected NodeObject createNodeObject ()
    {
        return new CrowdNodeObject();
    }

    @Override // documentation inherited
    protected ClientInfo createClientInfo ()
    {
        return new CrowdClientInfo();
    }

    @Override // documentation inherited
    protected void initClientInfo (PresentsClient client, ClientInfo info)
    {
        super.initClientInfo(client, info);
        ((CrowdClientInfo)info).visibleName =
            ((BodyObject)client.getClientObject()).getVisibleName();
    }

    @Override // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // register and initialize our invocation service
        CrowdNodeObject cnobj = (CrowdNodeObject)_nodeobj;
        cnobj.setCrowdPeerService(
            (CrowdPeerMarshaller)CrowdServer.invmgr.registerDispatcher(
                new CrowdPeerDispatcher(this)));

        // register ourselves as a tell forwarder
        CrowdServer.chatprov.setTellForwarder(this);
    }
}
