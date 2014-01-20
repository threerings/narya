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

import java.io.IOException;
import java.io.InputStream;

import com.threerings.io.ObjectInputStream;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObjectInputStream;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.Communicator;
import com.threerings.presents.client.ObserverOps;
import com.threerings.presents.client.SessionObserver;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.UpstreamMessage;

import static com.threerings.presents.Log.log;

/**
 * Provides Client {@link Communicator} services using non-blocking I/O and the connection manager.
 */
public class ServerCommunicator extends Communicator
{
    public ServerCommunicator (Client client, PresentsConnectionManager conmgr,
            RootDObjectManager rootmgr)
    {
        super(client);
        _conmgr = conmgr;
        _rootmgr = rootmgr;
    }

    @Override // from Communicator
    public void logon ()
    {
        // make sure things are copacetic
        if (_conn != null) {
            throw new RuntimeException("Communicator already started.");
        }

        // we assume server entities have no firewall issues and can connect on the first port
        try {
            PresentsConnection conn = new PresentsConnection() {
                @Override public void postMessage (Message msg) {
                    super.postMessage(msg);
                    // outgoing traffic on this connection is used to prevent idleness
                    // TODO: shouldn't PongResponse handle this?
                    _lastEvent = System.currentTimeMillis();
                }

                @Override public void connectFailure (IOException ioe) {
                    _logonError = ioe; // report this as a logon failure
                    super.connectFailure(ioe);
                }

                @Override public void networkFailure (final IOException ioe) {
                    notifyClientObservers(new ObserverOps.Client(_client) {
                        @Override protected void notify (ClientObserver obs) {
                            obs.clientConnectionFailed(_client, ioe);
                        }
                    });
                    super.networkFailure(ioe);
                }

                @Override protected ObjectInputStream createObjectInputStream (InputStream src) {
                    return new ClientObjectInputStream(_client, src);
                }

                @Override protected void closeSocket () {
                    super.closeSocket();
                    shutdown();
                }
            };
            conn.setMessageHandler(new PresentsConnection.MessageHandler() {
                public void handleMessage (Message message) {
                    try {
                        // our first message will always be an auth response
                        gotAuthResponse((AuthResponse)message);
                    } catch (Exception e) {
                        _logonError = e;
                        shutdown();
                    }
                }
            });
            _conmgr.openOutgoingConnection(conn, _client.getHostname(), _client.getPorts()[0]);
            _conn = conn;
            if (_loader != null) {
                _conn.setClassLoader(_loader);
            }

            // now send our auth request
            postMessage(new AuthRequest(_client.getCredentials(), _client.getVersion(),
                                        _client.getBootGroups()));

        } catch (IOException ioe) {
            _logonError = ioe;
            shutdown();
        }
    }

    @Override // from Communicator
    public void logoff ()
    {
        if (_conn != null) {
            _conn.postMessage(new LogoffRequest());
            _conn.asyncClose();
            _conn = null;
        }
    }

    @Override // from Communicator
    public void gotBootstrap ()
    {
        // nothing needed
    }

    @Override // from Communicator
    public void postMessage (final UpstreamMessage msg)
    {
        // if we're not on the main dobjmgr thread, we need to get there
        if (!_rootmgr.isDispatchThread()) {
            _rootmgr.postRunnable(new Runnable() {
                public void run () {
                    postMessage(msg);
                }
            });
            return;
        }

        if (_conn == null) {
            log.info("Dropping message for lack of connection.", "client", _client, "msg", msg);
            return;
        }

        // pass this message along to our connection
        _conn.postMessage(msg);

        // we cheat a bit and claim that we "wrote" when we post our messages so that we don't have
        // to modify the connection manager to call a method on Connection every time it writes a
        // message from the queue
        updateWriteStamp();
    }

    @Override // from Communicator
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        if (_conn != null) {
            _conn.setClassLoader(loader);
        }
    }

    @Override // from Communicator
    protected synchronized void logonSucceeded (AuthResponseData data)
    {
        super.logonSucceeded(data);

        // now we can route all messages to the ClientDObjectMgr
        _conn.setMessageHandler(new PresentsConnection.MessageHandler() {
            public void handleMessage (Message message) {
                processMessage(message);
            }
        });
    }

    protected void shutdown ()
    {
        if (_logonError == null) {
            // we were logged on successfully, so report didLogoff first
            notifyClientObservers(new ObserverOps.Session(_client) {
                @Override protected void notify (SessionObserver obs) {
                    obs.clientDidLogoff(_client);
                }
            });
        }
        clientCleanup(_logonError);
    }

    protected PresentsConnectionManager _conmgr;
    protected RootDObjectManager _rootmgr;
    protected PresentsConnection _conn;
    protected ClassLoader _loader;
    protected Exception _logonError;
}
