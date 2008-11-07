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

package com.threerings.presents.server.net;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.Message;

import static com.threerings.presents.Log.log;

/**
 * The authing connection manages the client connection until authentication has completed (for
 * better or for worse).
 */
public class AuthingConnection extends Connection
{
    public AuthingConnection ()
    {
        setMessageHandler(new MessageHandler() {
            public void handleMessage (Message msg) {
                try {
                    // keep a handle on our auth request
                    _authreq = (AuthRequest)msg;
                    // post ourselves for processing by the authmgr
                    _cmgr.authenticateConnection(AuthingConnection.this);
                } catch (ClassCastException cce) {
                    log.warning("Received non-authreq message during authentication process",
                                "conn", AuthingConnection.this, "msg", msg);
                }
            }
        });
    }

    /**
     * Returns a reference to the auth request currently being processed.
     */
    public AuthRequest getAuthRequest ()
    {
        return _authreq;
    }

    /**
     * Returns the auth response delivered to the client (only valid after the auth request has
     * been processed.
     */
    public AuthResponse getAuthResponse ()
    {
        return _authrsp;
    }

    /**
     * Stores a reference to the auth response delivered to this connection. This is called by the
     * auth manager after delivering the auth response to the client.
     */
    public void setAuthResponse (AuthResponse authrsp)
    {
        _authrsp = authrsp;
    }

    @Override
    public String toString ()
    {
        return "[mode=AUTHING, addr=" + getInetAddress() + "]";
    }

    protected AuthRequest _authreq;
    protected AuthResponse _authrsp;
}
