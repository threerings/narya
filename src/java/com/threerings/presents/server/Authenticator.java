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

package com.threerings.presents.server;

import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;

import com.threerings.presents.server.net.AuthingConnection;
import com.threerings.presents.server.net.ConnectionManager;

/**
 * The authenticator is a pluggable component of the authentication
 * framework. The base class handles the basic mechanics of authentication
 * and a system would extend the base authenticator and add code that does
 * the actual client authentication.
 */
public abstract class Authenticator
{
    /**
     * Called by the connection manager to give us a reference to it for
     * reporting authenticated connections.
     */
    public void setConnectionManager (ConnectionManager conmgr)
    {
        _conmgr = conmgr;
    }

    /**
     * Called by the connection management code when an authenticating
     * connection has received its authentication request from the client.
     * This method must return immediately as it is called on the
     * connection manager thread. If it is possible to authenticate the
     * connection immediately, it may do so, but more likely it will fire
     * off a task on the {@link PresentsServer#invoker} to perform the
     * authentication. When the authentication is complete, the
     * authenticator implementation should call {@link
     * #connectionWasAuthenticated} <em>from the distributed object
     * thread</em>.
     */
    public abstract void authenticateConnection (AuthingConnection conn);

    /**
     * This is called by authenticator implementations when they have
     * completed the authentication process. It will deliver the response
     * to the user and let the connection manager know to report the
     * authentication if it succeeded. <em>Note:</em> this method should
     * only be called on the distributed object thread as all messages
     * being sent to the client should originate therefrom.
     */
    protected void connectionWasAuthenticated (
        AuthingConnection conn, AuthResponse rsp)
    {
        // now ship the response back
        conn.postMessage(rsp);

        // stuff a reference to the auth response into the connection so
        // that we have access to it later in the authentication process
        conn.setAuthResponse(rsp);

        // if the authentication request was granted, let the connection
        // manager know that we just authed
        if (AuthResponseData.SUCCESS.equals(rsp.getData().code)) {
            _conmgr.connectionDidAuthenticate(conn);
        }
    }

    /** The connection manager with which we're working. */
    protected ConnectionManager _conmgr;
}
