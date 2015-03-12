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

package com.threerings.presents.server.net;

import static com.threerings.presents.Log.log;

import java.io.IOException;
import java.security.PrivateKey;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.net.AESAuthRequest;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.PublicKeyCredentials;
import com.threerings.presents.net.SecureRequest;
import com.threerings.presents.net.SecureResponse;
import com.threerings.util.Name;

/**
 * The authing connection manages the client connection until authentication has completed (for
 * better or for worse).
 */
public class AuthingConnection extends PresentsConnection
{
    public AuthingConnection ()
    {
        setMessageHandler(new MessageHandler() {
            public void handleMessage (Message msg) {
                if (_serverSecret == null) {
                    // first see if the client is trying to start secure authentication
                    try {
                        SecureRequest secreq = (SecureRequest)msg;
                        PrivateKey key = _pcmgr.getPrivateKey();
                        // fail quickly if we don't support secure connections
                        if (key == null) {
                            safePostMessage(new SecureResponse(AuthCodes.FAILED_TO_SECURE));
                        } else {
                            // generate a server key and encode it using the client key
                            SecureResponse resp = new SecureResponse();
                            PublicKeyCredentials pkcreds =
                                    (PublicKeyCredentials)secreq.getCredentials();
                            _clientSecureVersion = pkcreds.getSecureVersion();
                            _serverSecret = resp.createSecret(pkcreds, key, 16);
                            safePostMessage(resp);
                        }
                        return;
                    } catch (ClassCastException cce) {
                        // Client didn't request a secure channel so proceed with normal
                        // authentication
                    }
                } else {
                    try {
                        ((AESAuthRequest)msg).decrypt(_serverSecret);

                    } catch (ClassCastException cce) {
                        log.warning(
                            "Received non-encrypted request during secure authentication process",
                            "conn", AuthingConnection.this, "msg", msg);
                    } catch (ClassNotFoundException cnfe) {
                        log.warning(
                            "Failed to decrypt request during secure authentication process",
                            "conn", AuthingConnection.this, "msg", msg, cnfe);
                        safePostMessage(new SecureResponse(AuthCodes.FAILED_TO_SECURE));
                        return;
                    } catch (IOException ioe) {
                        log.warning(
                            "Failed to decrypt request during secure authentication process",
                            "conn", AuthingConnection.this, "msg", msg, ioe);
                        safePostMessage(new SecureResponse(AuthCodes.FAILED_TO_SECURE));
                        return;
                    }
                }
                try {
                    // keep a handle on our auth request
                    _authreq = (AuthRequest)msg;
                } catch (ClassCastException cce) {
                    log.warning("Received non-authreq message during authentication process",
                        "conn", AuthingConnection.this, "msg", msg);
                }

                if (_authreq != null) {
                    // post ourselves for processing by the authmgr
                    _pcmgr.authenticateConnection(AuthingConnection.this);
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

    /**
     * Returns the username that uniquely identifies this authenticated session. This will be used
     * to map {@code Name -> PresentsSession} in the {@code ClientManager} and used elsewhere.
     */
    public Name getAuthName ()
    {
        return _authname;
    }

    /**
     * During the authentication process, the authenticator must establish the client's
     * authentication username and configure it via this method.
     */
    public void setAuthName (Name authname)
    {
        _authname = authname;
    }

    @Override
    public String toString ()
    {
        return "[mode=AUTHING, addr=" + getInetAddress() + "]";
    }

    /**
     * Callable from non-dobjmgr thread, this queues up a runnable on the dobjmgr thread to post
     * the supplied message to this client.
     */
    protected final void safePostMessage (final DownstreamMessage msg)
    {
        _pcmgr._omgr.postRunnable(new Runnable() {
            public void run () {
                postMessage(msg);
            }
        });
    }

    protected AuthRequest _authreq;
    protected AuthResponse _authrsp;
    protected Name _authname;

    /** The random secret generating for this connection. */
    protected byte[] _serverSecret;

    /** The secure version for our connecting client. */
    protected int _clientSecureVersion;
}
