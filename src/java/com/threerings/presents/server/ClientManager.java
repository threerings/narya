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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.samskivert.util.Interval;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.server.net.*;

/**
 * The client manager is responsible for managing the clients (surprise,
 * surprise) which are slightly more than just connections. Clients
 * persist in the absence of connections in case a user goes bye bye
 * unintentionally and wants to reconnect and continue their session.
 *
 * <p> The client manager operates with thread safety because it is called
 * both from the conmgr thread (to notify of connections showing up or
 * going away) and from the dobjmgr thread (when clients are given the
 * boot for application-defined reasons).
 */
public class ClientManager
    implements ConnectionObserver, ClientResolutionListener,
               PresentsServer.Reporter, PresentsServer.Shutdowner
{
    /**
     * Used by {@link #applyToClient}.
     */
    public static interface ClientOp
    {
        /**
         * Called with the resolved client object.
         */
        public void apply (ClientObject clobj);

        /**
         * Called if the client resolution fails.
         */
        public void resolutionFailed (Exception e);
    }

    /**
     * Constructs a client manager that will interact with the supplied
     * connection manager.
     */
    public ClientManager (ConnectionManager conmgr)
    {
        // register ourselves as a connection observer
        conmgr.addConnectionObserver(this);

        // start up an interval that will check for expired clients and
        // flush them from the bowels of the server
        new Interval(PresentsServer.omgr) {
            public void expired () {
                flushClients();
            }
        }.schedule(CLIENT_FLUSH_INTERVAL, true);

        // register as a "state of server" reporter and a shutdowner
        PresentsServer.registerReporter(this);
        PresentsServer.registerShutdowner(this);
    }

    // documentation inherited from interface
    public void shutdown ()
    {
        Log.info("Client manager shutting down " +
                 "[ccount=" + _usermap.size() + "].");

        // inform all of our clients that they are being shut down
        for (Iterator iter = _usermap.values().iterator(); iter.hasNext(); ) {
            PresentsClient pc = (PresentsClient)iter.next();
            try {
                pc.shutdown();
            } catch (Exception e) {
                Log.warning("Client choked in shutdonw() [client=" +
                            StringUtil.safeToString(pc) + "].");
                Log.logStackTrace(e);
            }
        }
    }

    /**
     * Configures the client manager with a factory for creating {@link
     * PresentsClient} and {@link ClientResolver} classes for authenticated
     * client connections.
     */
    public void setClientFactory (ClientFactory factory)
    {
        _factory = factory;
    }

    /**
     * Returns the {@link ClientFactory} currently in use.
     */
    public ClientFactory getClientFactory ()
    {
        return _factory;
    }

    /**
     * Return the number of client resolutions are currently happening.
     */
    public int getOutstandingResolutionCount ()
    {
        return _penders.size();
    }

    /**
     * Returns the number of client sessions (some may be disconnected).
     */
    public int getClientCount ()
    {
        return _usermap.size();
    }

    /**
     * Returns the number of connected clients.
     */
    public int getConnectionCount ()
    {
        return _conmap.size();
    }

    /**
     * Enumerates all active client objects.
     */
    public Iterator enumerateClientObjects ()
    {
        return _objmap.values().iterator();
    }

    /**
     * Returns the client instance that manages the client session for the
     * specified authentication username or null if that client is not
     * currently connected to the server.
     */
    public PresentsClient getClient (Name authUsername)
    {
        return _usermap.get(authUsername);
    }

    /**
     * Returns the client object associated with the specified username.
     * This will return null unless the client object is resolved for some
     * reason (like they are logged on).
     */
    public ClientObject getClientObject (Name username)
    {
        return _objmap.get(username);
    }

    /**
     * Resolves the specified client, applies the supplied client
     * operation to them and releases the client.
     */
    public void applyToClient (Name username, ClientOp clop)
    {
        resolveClientObject(username, new ClientOpResolver(clop));
    }

    /**
     * Requests that the client object for the specified user be resolved.
     * If the client object is already resolved, the request will be
     * processed immediately, otherwise the appropriate client object will
     * be instantiated and populated by the registered client resolver
     * (which may involve talking to databases). <em>Note:</em> this
     * <b>must</b> be paired with a call to {@link #releaseClientObject}
     * when the caller is finished with the client object.
     */
    public synchronized void resolveClientObject (
        Name username, final ClientResolutionListener listener)
    {
        // look to see if the client object is already resolved
        ClientObject clobj = _objmap.get(username);
        if (clobj != null) {
            clobj.reference();
            listener.clientResolved(username, clobj);
            return;
        }

        // look to see if it's currently being resolved
        ClientResolver clr = _penders.get(username);
        if (clr != null) {
            // throw this guy onto the bandwagon
            clr.addResolutionListener(listener);
            return;
        }

        try {
            // create a client resolver instance which will create our
            // client object, populate it and notify the listeners
            clr = _factory.createClientResolver(username);
            clr.init(username);
            clr.addResolutionListener(this);
            clr.addResolutionListener(listener);
            _penders.put(username, clr);

            // request that the appropriate client object be created by the
            // dobject manager which starts the whole business off
            @SuppressWarnings("unchecked") Class<ClientObject> cclass =
                (Class<ClientObject>)clr.getClientObjectClass();
            PresentsServer.omgr.createObject(cclass, clr);

        } catch (Exception e) {
            // let the listener know that we're hosed
            listener.resolutionFailed(username, e);
        }
    }

    /**
     * Releases a client object that was obtained via a call to {@link
     * #resolveClientObject}. If this caller is the last reference, the
     * object will be flushed and destroyed.
     */
    public void releaseClientObject (Name username)
    {
        ClientObject clobj = _objmap.get(username);
        if (clobj == null) {
            Log.warning("Requested to release unmapped client object " +
                        "[username=" + username + "].");
            Thread.dumpStack();
            return;
        }

        // decrement the reference count and stop here if there are
        // remaining references
        if (clobj.release()) {
            return;
        }

        Log.debug("Destroying client " + clobj.who() + ".");

        // we're all clear to go; remove the mapping
        _objmap.remove(username);

        // and destroy the object itself
        PresentsServer.omgr.destroyObject(clobj.getOid());
    }

    // documentation inherited from interface ClientResolutionListener
    public synchronized void clientResolved (Name username, ClientObject clobj)
    {
        // stuff the object into the mapping table
        _objmap.put(username, clobj);

        // and remove the resolution listener
        _penders.remove(username);
    }

    // documentation inherited from interface ClientResolutionListener
    public synchronized void resolutionFailed (Name username, Exception reason)
    {
        // clear out their pending record
        _penders.remove(username);
    }

    // documentation inherited
    public synchronized void connectionEstablished (
        Connection conn, AuthRequest req, AuthResponse rsp)
    {
        Credentials creds = req.getCredentials();
        Name username = creds.getUsername();

        // see if a client is already registered with these credentials
        PresentsClient client = getClient(username);

        if (client != null) {
            Log.info("Resuming session [username=" + username +
                     ", conn=" + conn + "].");
            client.resumeSession(conn);

        } else {
            Log.info("Session initiated [username=" + username +
                     ", conn=" + conn + "].");
            // create a new client and stick'em in the table
            client = _factory.createClient(req);
            client.startSession(this, creds, conn, rsp.authdata);

            // map their client instance
            _usermap.put(username, client);
        }

        // map this connection to this client
        _conmap.put(conn, client);
    }

    // documentation inherited
    public synchronized void connectionFailed (
        Connection conn, IOException fault)
    {
        // remove the client from the connection map
        PresentsClient client = _conmap.remove(conn);
        if (client != null) {
            Log.info("Unmapped failed client [client=" + client +
                     ", conn=" + conn + ", fault=" + fault + "].");
            // let the client know the connection went away
            client.wasUnmapped();
            // and let the client know things went haywire
            client.connectionFailed(fault);

        } else if (!(conn instanceof AuthingConnection)) {
            Log.info("Unmapped connection failed? [conn=" + conn +
                     ", fault=" + fault + "].");
            Thread.dumpStack();
        }
    }

    // documentation inherited
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the client from the connection map
        PresentsClient client = _conmap.remove(conn);
        if (client != null) {
            Log.debug("Unmapped client [client=" + client +
                      ", conn=" + conn + "].");
            // let the client know the connection went away
            client.wasUnmapped();

        } else {
            Log.info("Closed unmapped connection '" + conn + "'. " +
                     "Client probably not yet authenticated.");
        }
    }

    // documentation inherited from interface PresentsServer.Reporter
    public void appendReport (
        StringBuilder report, long now, long sinceLast, boolean reset)
    {
        report.append("* presents.ClientManager:\n");
        report.append("- Sessions: ");
        report.append(_usermap.size()).append(" total, ");
        report.append(_conmap.size()).append(" connected, ");
        report.append(_penders.size()).append(" pending\n");
        report.append("- Mapped users: ").append(_objmap.size()).append("\n");
    }

    /**
     * Called by the client instance when the client requests a logoff.
     * This is called from the conmgr thread.
     */
    synchronized void clientDidEndSession (PresentsClient client)
    {
        // remove the client from the username map
        Credentials creds = client.getCredentials();
        PresentsClient rc = _usermap.remove(creds.getUsername());

        // sanity check just because we can
        if (rc == null) {
            Log.warning("Unregistered client ended session " + client + ".");
            Thread.dumpStack();
        } else if (rc != client) {
            Log.warning("Different clients with same username!? " +
                        "[c1=" + rc + ", c2=" + client + "].");
        } else {
            Log.info("Ending session " + client + ".");
        }
    }

    /**
     * Called once per minute to check for clients that have been
     * disconnected too long and forcibly end their sessions.
     */
    protected void flushClients ()
    {
        ArrayList<PresentsClient> victims = null;
        long now = System.currentTimeMillis();

        // first build a list of our victims (we can't flush clients
        // directly while iterating due to risk of a
        // ConcurrentModificationException)
        for (PresentsClient client : _usermap.values()) {
            if (client.checkExpired(now)) {
                if (victims == null) {
                    victims = new ArrayList<PresentsClient>();
                }
                victims.add(client);
            }
        }

        if (victims != null) {
            for (PresentsClient client : victims) {
                try {
                    Log.info("Client expired, ending session " +
                        "[client=" + client +
                        ", dtime=" + (now-client.getNetworkStamp()) + "ms].");
                    client.endSession();
                } catch (Exception e) {
                    Log.warning("Choke while flushing client " +
                                "[victim=" + client + "].");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    /** Used by {@link #applyToClient}. */
    protected class ClientOpResolver
        implements ClientResolutionListener
    {
        public ClientOpResolver (ClientOp clop)
        {
            _clop = clop;
        }

        // documentation inherited from interface
        public void clientResolved (Name username, ClientObject clobj)
        {
            try {
                _clop.apply(clobj);

            } catch (Exception e) {
                Log.warning("Client op failed [username=" + username +
                            ", clop=" + _clop + "].");
                Log.logStackTrace(e);

            } finally {
                releaseClientObject(username);
            }
        }

        // documentation inherited from interface
        public void resolutionFailed (Name username, Exception reason)
        {
            _clop.resolutionFailed(reason);
        }

        protected ClientOp _clop;
    }

    /** A mapping from auth username to client instances. */
    protected HashMap<Name,PresentsClient> _usermap =
        new HashMap<Name,PresentsClient>();

    /** A mapping from connections to client instances. */
    protected HashMap<Connection,PresentsClient> _conmap =
        new HashMap<Connection,PresentsClient>();

    /** A mapping from usernames to client object instances. */
    protected HashMap<Name,ClientObject> _objmap =
        new HashMap<Name,ClientObject>();

    /** A mapping of pending client resolvers. */
    protected HashMap<Name,ClientResolver> _penders =
        new HashMap<Name,ClientResolver>();

    /** The client class in use. */
    protected ClientFactory _factory = ClientFactory.DEFAULT;

    /** The frequency with which we check for expired clients. */
    protected static final long CLIENT_FLUSH_INTERVAL = 60 * 1000L;
}
