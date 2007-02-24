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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.UpstreamMessage;

/**
 * The authing connection manages the client connection until
 * authentication has completed (for better or for worse).
 */
public class AuthingConnection extends Connection
    implements MessageHandler
{
    /**
     * Creates a new authing connection object that will manage the
     * authentication process for the suppled client socket.
     */
    public AuthingConnection (ConnectionManager cmgr, SelectionKey selkey,
                              SocketChannel channel)
        throws IOException
    {
        super(cmgr, selkey, channel, System.currentTimeMillis());

        // we are our own message handler
        setMessageHandler(this);
    }

    /**
     * Called when a new message has arrived from the client.
     */
    public void handleMessage (UpstreamMessage msg)
    {
        try {
            // keep a handle on our auth request
            _authreq = (AuthRequest)msg;

            // post ourselves for processing by the authmgr
            _cmgr.getAuthenticator().authenticateConnection(this);

        } catch (ClassCastException cce) {
            Log.warning("Received non-authreq message during " +
                        "authentication process [conn=" + this +
                        ", msg=" + msg + "].");
        }
    }

    /**
     * Returns a reference to the auth request currently being processed
     * by this authing connection.
     */
    public AuthRequest getAuthRequest ()
    {
        return _authreq;
    }

    /**
     * Returns the auth response delivered to the client (only valid after
     * the auth request has been processed.
     */
    public AuthResponse getAuthResponse ()
    {
        return _authrsp;
    }

    /**
     * Stores a reference to the auth response delivered to this
     * connection. This is called by the auth manager after delivering the
     * auth response to the client.
     */
    public void setAuthResponse (AuthResponse authrsp)
    {
        _authrsp = authrsp;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[mode=AUTHING, addr=" +
            StringUtil.toString(_channel.socket().getInetAddress()) + "]";
    }

    protected AuthRequest _authreq;
    protected AuthResponse _authrsp;
}
