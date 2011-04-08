//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;

/**
 * A peer manager with hooks for testing.
 */
@Singleton
public class TestPeerManager extends PeerManager
{
    public interface Callback<T>
    {
        void apply (T value);
    }

    @Inject
    public TestPeerManager (Lifecycle cycle)
    {
        super(cycle);
    }

    public void setOnConnected (Callback<NodeObject> onConnected)
    {
        _onConnected = onConnected;
    }

    public void setOnClientLoggedOn (Callback<ClientInfo> onClientLoggedOn)
    {
        _onClientLoggedOn = onClientLoggedOn;
    }

    public void setOnClientLoggedOff (Callback<ClientInfo> onClientLoggedOff)
    {
        _onClientLoggedOff = onClientLoggedOff;
    }

    @Override
    protected void connectedToPeer (PeerNode peer)
    {
        super.connectedToPeer(peer);
        if (_onConnected != null) {
            _onConnected.apply(peer.nodeobj);
        }
    }

    @Override
    protected void clientLoggedOn (String nodeName, ClientInfo clinfo)
    {
        super.clientLoggedOn(nodeName, clinfo);
        if (_onClientLoggedOn != null) {
            _onClientLoggedOn.apply(clinfo);
        }
    }

    @Override
    protected void clientLoggedOff (String nodeName, ClientInfo clinfo)
    {
        super.clientLoggedOff(nodeName, clinfo);
        if (_onClientLoggedOff != null) {
            _onClientLoggedOff.apply(clinfo);
        }
    }

    protected Callback<NodeObject> _onConnected;
    protected Callback<ClientInfo> _onClientLoggedOn, _onClientLoggedOff;
}
