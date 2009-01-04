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

package com.threerings.presents.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.IOException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.Interval;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.server.net.AuthingConnection;
import com.threerings.presents.server.net.Connection;

import static com.threerings.presents.Log.log;

/**
 * The client manager is responsible for managing the clients (surprise, surprise) which are
 * slightly more than just connections. Clients persist in the absence of connections in case a
 * user goes bye bye unintentionally and wants to reconnect and continue their session.
 *
 * <p> The client manager operates with thread safety because it is called both from the conmgr
 * thread (to notify of connections showing up or going away) and from the dobjmgr thread (when
 * clients are given the boot for application-defined reasons).
 */
@Singleton
public class ClientManager
    implements ClientResolutionListener, ReportManager.Reporter, ShutdownManager.Shutdowner
{
    /**
     * Used by {@link ClientManager#applyToClient}.
     */
    public static interface ClientOp
    {
        /**
         * Called with the resolved client object.
         */
        void apply (ClientObject clobj);

        /**
         * Called if the client resolution fails.
         */
        void resolutionFailed (Exception e);
    }

    /**
     * Used by entites that wish to track when clients initiate and end sessions on this server.
     */
    public static interface ClientObserver
    {
        /**
         * Called when a client has authenticated and been resolved and has started their session.
         */
        void clientSessionDidStart (PresentsSession session);

        /**
         * Called when a client has logged off or been forcibly logged off due to inactivity and
         * has thus ended their session.
         */
        void clientSessionDidEnd (PresentsSession session);
    }

    /**
     * Constructs a client manager that will interact with the supplied connection manager.
     */
    @Inject public ClientManager (ReportManager repmgr, ShutdownManager shutmgr)
    {
        repmgr.registerReporter(this);
        shutmgr.registerShutdowner(this);

        // start up an interval that will check for expired clients and flush them from the bowels
        // of the server
        new Interval(_omgr) {
            @Override
            public void expired () {
                flushClients();
            }
        }.schedule(CLIENT_FLUSH_INTERVAL, true);
    }

    /**
     * Configures the injector we'll use to resolve dependencies for {@link PresentsSession}
     * instances.
     */
    public void setInjector (Injector injector)
    {
        _injector = injector;
    }

    // from interface ShutdownManager.Shutdowner
    public void shutdown ()
    {
        log.info("Client manager shutting down [ccount=" + _usermap.size() + "].");

        // inform all of our clients that they are being shut down
        synchronized (_usermap) {
            for (PresentsSession pc : _usermap.values()) {
                try {
                    pc.shutdown();
                } catch (Exception e) {
                    log.warning("Client choked in shutdown() [client=" +
                            StringUtil.safeToString(pc) + "].", e);
                }
            }
        }
    }

    /**
     * Configures the client manager with a factory for creating {@link PresentsSession} and {@link
     * ClientResolver} classes for authenticated client connections.
     */
    public void setSessionFactory (SessionFactory factory)
    {
        _factory = factory;
    }

    /**
     * Returns the {@link SessionFactory} currently in use.
     */
    public SessionFactory getSessionFactory ()
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
        synchronized (_usermap) {
            return _usermap.size();
        }
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
    public Iterator<ClientObject> enumerateClientObjects ()
    {
        return _objmap.values().iterator();
    }

    /**
     * Registers an observer that will be notified when clients start and end their sessions.
     */
    public void addClientObserver (ClientObserver observer)
    {
        _clobservers.add(observer);
    }

    /**
     * Removes an observer previously registered with {@link #addClientObserver}.
     */
    public void removeClientObserver (ClientObserver observer)
    {
        _clobservers.remove(observer);
    }

    /**
     * Returns the client instance that manages the client session for the specified authentication
     * username or null if that client is not currently connected to the server.
     */
    public PresentsSession getClient (Name authUsername)
    {
        synchronized (_usermap) {
            return _usermap.get(authUsername);
        }
    }

    /**
     * Returns the client object associated with the specified username.  This will return null
     * unless the client object is resolved for some reason (like they are logged on).
     */
    public ClientObject getClientObject (Name username)
    {
        return _objmap.get(username);
    }

    /**
     * Resolves the specified client, applies the supplied client operation to them and releases
     * the client.
     */
    public void applyToClient (Name username, ClientOp clop)
    {
        resolveClientObject(username, new ClientOpResolver(clop));
    }

    /**
     * Requests that the client object for the specified user be resolved.  If the client object is
     * already resolved, the request will be processed immediately, otherwise the appropriate
     * client object will be instantiated and populated by the registered client resolver (which
     * may involve talking to databases). <em>Note:</em> this <b>must</b> be paired with a call to
     * {@link #releaseClientObject} when the caller is finished with the client object.
     */
    public synchronized void resolveClientObject (
        final Name username, final ClientResolutionListener listener)
    {
        // look to see if the client object is already resolved
        final ClientObject clobj = _objmap.get(username);
        if (clobj != null) {
            // report that the client is resolved on the dobjmgr thread to provide equivalent
            // behavior to the case where we actually have to do the resolution
            clobj.reference();
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    listener.clientResolved(username, clobj);
                }
            });
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
            // create a client resolver instance which will create our client object, populate it
            // and notify the listeners
            clr = _injector.getInstance(_factory.getClientResolverClass(username));
            clr.init(username);
            clr.addResolutionListener(this);
            clr.addResolutionListener(listener);
            _penders.put(username, clr);

            // create and register our client object and give it back to the client resolver; we
            // need to do this on the dobjmgr thread since we're registering an object
            final ClientResolver fclr = clr;
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    ClientObject clobj = fclr.createClientObject();
                    clobj.setLocal(ClientLocal.class, fclr.createLocalAttribute());
                    clobj.setPermissionPolicy(fclr.createPermissionPolicy());
                    fclr.objectAvailable(_omgr.registerObject(clobj));
                }
            });

        } catch (Exception e) {
            // let the listener know that we're hosed
            listener.resolutionFailed(username, e);
        }
    }

    /**
     * Releases a client object that was obtained via a call to {@link #resolveClientObject}. If
     * this caller is the last reference, the object will be flushed and destroyed.
     */
    public void releaseClientObject (Name username)
    {
        ClientObject clobj = _objmap.get(username);
        if (clobj == null) {
            log.warning("Requested to release unmapped client object", "username", username,
                        new Exception());
            return;
        }

        // decrement the reference count and stop here if there are remaining references
        if (clobj.release()) {
            return;
        }

        log.debug("Destroying client " + clobj.who() + ".");

        // we're all clear to go; remove the mapping
        _objmap.remove(username);

        // and destroy the object itself
        _omgr.destroyObject(clobj.getOid());
    }

    /**
     * Renames a currently connected client from <code>oldname</code> to <code>newname</code>.
     *
     * @return true if the client was found and renamed.
     */
    protected boolean renameClientObject (Name oldname, Name newname)
    {
        ClientObject clobj = _objmap.remove(oldname);
        if (clobj == null) {
            log.warning("Requested to rename unmapped client object", "username", oldname,
                        new Exception());
            return false;
        }
        _objmap.put(newname, clobj);
        return true;
    }

    // documentation inherited from interface ClientResolutionListener
    public synchronized void clientResolved (Name username, ClientObject clobj)
    {
        // because we added ourselves as a client resolution listener, the client object reference
        // count was increased, but we're not a real resolver (who would have to call
        // releaseClientObject() to release their reference), so we release our reference
        // immediately
        clobj.release();

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

    /**
     * Called by the connection manager to let us know when a new connection has been established.
     */
    public synchronized void connectionEstablished (
        Connection conn, AuthRequest req, AuthResponse rsp)
    {
        Credentials creds = req.getCredentials();
        Name username = creds.getUsername();

        // see if a client is already registered with these credentials
        PresentsSession client = getClient(username);

        if (client != null) {
            log.info("Resuming session [username=" + username + ", conn=" + conn + "].");
            client.resumeSession(req, conn);

        } else {
            log.info("Session initiated [username=" + username + ", conn=" + conn + "].");
            // create a new client and stick'em in the table
            client = _injector.getInstance(_factory.getSessionClass(req));
            client.startSession(req, conn, rsp.authdata);

            // map their client instance
            synchronized (_usermap) {
                _usermap.put(username, client);
            }
        }

        // map this connection to this client
        _conmap.put(conn, client);
    }

    /**
     * Called by the connection manager to let us know when a connection has failed.
     */
    public synchronized void connectionFailed (Connection conn, IOException fault)
    {
        // remove the client from the connection map
        PresentsSession client = _conmap.remove(conn);
        if (client != null) {
            log.info("Unmapped failed client [client=" + client + ", conn=" + conn +
                     ", fault=" + fault + "].");
            // let the client know the connection went away
            client.wasUnmapped();
            // and let the client know things went haywire
            client.connectionFailed(fault);

        } else if (!(conn instanceof AuthingConnection)) {
            log.info("Unmapped connection failed?", "conn", conn, "fault", fault, new Exception());
        }
    }

    /**
     * Called by the connection manager to let us know when a connection has been closed.
     */
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the client from the connection map
        PresentsSession client = _conmap.remove(conn);
        if (client != null) {
            log.debug("Unmapped client [client=" + client + ", conn=" + conn + "].");
            // let the client know the connection went away
            client.wasUnmapped();

        } else {
            log.info("Closed unmapped connection '" + conn + "'. " +
                     "Client probably not yet authenticated.");
        }
    }

    // documentation inherited from interface ReportManager.Reporter
    public void appendReport (StringBuilder report, long now, long sinceLast, boolean reset)
    {
        report.append("* presents.ClientManager:\n");
        report.append("- Sessions: ");
        synchronized (_usermap) {
            report.append(_usermap.size()).append(" total, ");
        }
        report.append(_conmap.size()).append(" connected, ");
        report.append(_penders.size()).append(" pending\n");
        report.append("- Mapped users: ").append(_objmap.size()).append("\n");
    }

    /**
     * Called by the client instance when it has started its session. This is called from the
     * dobjmgr thread.
     */
    protected void clientSessionDidStart (final PresentsSession client)
    {
        // let the observers know
        _clobservers.apply(new ObserverList.ObserverOp<ClientObserver>() {
            public boolean apply (ClientObserver observer) {
                observer.clientSessionDidStart(client);
                return true;
            }
        });
    }

    /**
     * Called by the client instance when the client requests a logoff. This is called from the
     * dobjmgr thread.
     */
    protected void clientSessionDidEnd (final PresentsSession client)
    {
        // remove the client from the username map
        Name username = client.getCredentials().getUsername();
        PresentsSession rc;
        synchronized (_usermap) {
            rc = _usermap.remove(username);
        }

        // sanity check just because we can
        if (rc == null) {
            log.warning("Unregistered client ended session", "username", username, "client", client,
                        new Exception());
        } else if (rc != client) {
            log.warning("Different clients with same username!?", "c1", rc, "c2", client);
        } else {
            log.info("Ending session", "username", username, "client", client);
        }

        // notify the observers that the session is ended
        _clobservers.apply(new ObserverList.ObserverOp<ClientObserver>() {
            public boolean apply (ClientObserver observer) {
                observer.clientSessionDidEnd(client);
                return true;
            }
        });
    }

    /**
     * Called once per minute to check for clients that have been disconnected too long and
     * forcibly end their sessions.
     */
    protected void flushClients ()
    {
        List<PresentsSession> victims = Lists.newArrayList();
        long now = System.currentTimeMillis();

        // first build a list of our victims
        synchronized (_usermap) {
            for (PresentsSession client : _usermap.values()) {
                if (client.checkExpired(now)) {
                    victims.add(client);
                }
            }
        }

        // now end their sessions
        for (PresentsSession client : victims) {
            try {
                log.info("Client expired, ending session [client=" + client +
                         ", dtime=" + (now-client.getNetworkStamp()) + "ms].");
                client.endSession();
            } catch (Exception e) {
                log.warning("Choke while flushing client [victim=" + client + "].", e);
            }
        }
    }

    /** Used by {@link ClientManager#applyToClient}. */
    protected class ClientOpResolver
        implements ClientResolutionListener
    {
        public ClientOpResolver (ClientOp clop) {
            _clop = clop;
        }

        public void clientResolved (Name username, ClientObject clobj) {
            try {
                _clop.apply(clobj);

            } catch (Exception e) {
                log.warning("Client op failed [username=" + username +
                        ", clop=" + _clop + "].", e);

            } finally {
                releaseClientObject(username);
            }
        }

        public void resolutionFailed (Name username, Exception reason) {
            _clop.resolutionFailed(reason);
        }

        protected ClientOp _clop;
    }

    /** Used to resolve dependencies in {@link PresentsSession} instances that we create. */
    protected Injector _injector;

    /** A mapping from auth username to client instances. */
    protected Map<Name, PresentsSession> _usermap = Maps.newHashMap();

    /** A mapping from connections to client instances. */
    protected Map<Connection, PresentsSession> _conmap = Maps.newHashMap();

    /** A mapping from usernames to client object instances. */
    protected Map<Name, ClientObject> _objmap = Maps.newHashMap();

    /** A mapping of pending client resolvers. */
    protected Map<Name, ClientResolver> _penders = Maps.newHashMap();

    /** Lets us know what sort of client classes to use. */
    protected SessionFactory _factory = SessionFactory.DEFAULT;

    /** Tracks registered {@link ClientObserver}s. */
    protected ObserverList<ClientObserver> _clobservers = ObserverList.newSafeInOrder();

    // our injected dependencies
    @Inject protected PresentsDObjectMgr _omgr;

    /** The frequency with which we check for expired clients. */
    protected static final long CLIENT_FLUSH_INTERVAL = 60 * 1000L;
}
