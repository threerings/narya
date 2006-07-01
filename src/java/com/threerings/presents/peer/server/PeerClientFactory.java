//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.ClientFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsClient;

import com.threerings.presents.peer.net.PeerCreds;

/**
 * Handles resolution of peer servers and passes non-peer resolution requests
 * through to a normal factory.
 */
public class PeerClientFactory implements ClientFactory
{
    public PeerClientFactory (PeerManager peermgr, ClientFactory delegate)
    {
        _peermgr = peermgr;
        _delegate = delegate;
    }

    // documentation inherited from interface ClientFactory
    public PresentsClient createClient (AuthRequest areq)
    {
        if (areq.getCredentials() instanceof PeerCreds) {
            return new PeerClient(_peermgr);
        } else {
            return _delegate.createClient(areq);
        }
    }

    // documentation inherited from interface ClientFactory
    public ClientResolver createClientResolver (Name username)
    {
        if (username.toString().startsWith(PeerCreds.PEER_PREFIX)) {
            return new PeerClientResolver();
        } else {
            return _delegate.createClientResolver(username);
        }
    }

    protected PeerManager _peermgr;
    protected ClientFactory _delegate;
}
