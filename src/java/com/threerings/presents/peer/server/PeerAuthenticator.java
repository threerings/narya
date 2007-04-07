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

package com.threerings.presents.peer.server;

import com.samskivert.io.PersistenceException;

import com.samskivert.util.Invoker;

import com.threerings.presents.data.AuthCodes;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.net.AuthingConnection;

import com.threerings.presents.peer.net.PeerCreds;

import static com.threerings.presents.Log.log;

/**
 * Handles authentication of peer servers and passes non-peer authentication requests through to a
 * normal authenticator.
 */
public class PeerAuthenticator extends Authenticator
{
    /**
     * Creates an authenticator that will handle peer authentications and pass non-peer
     * authentications through to the supplied delegate.
     */
    public PeerAuthenticator (PeerManager nodemgr, Authenticator delegate)
    {
        _peermgr = nodemgr;
        _delegate = delegate;
    }

    @Override
    public void authenticateConnection (AuthingConnection conn)
    {
        // if this is a peer server, we check their credentials specially
        AuthRequest req = conn.getAuthRequest();
        if (req.getCredentials() instanceof PeerCreds) {
            super.authenticateConnection(conn);

        } else {
            // otherwise pass the request on to our delegate
            _delegate.authenticateConnection(conn);
        }
    }

    // from abstract Authenticator
    protected void processAuthentication (AuthingConnection conn, AuthResponse rsp)
        throws PersistenceException
    {
        // here, we are ONLY authenticating peers
        AuthRequest req = conn.getAuthRequest();
        PeerCreds pcreds = (PeerCreds) req.getCredentials();

        if (_peermgr.isAuthenticPeer(pcreds)) {
            rsp.getData().code = AuthResponseData.SUCCESS;

        } else {
            log.warning("Received invalid peer auth request? [creds=" + pcreds + "].");
            rsp.getData().code = AuthCodes.SERVER_ERROR;
        }
    }

    protected PeerManager _peermgr;
    protected Authenticator _delegate;
}
