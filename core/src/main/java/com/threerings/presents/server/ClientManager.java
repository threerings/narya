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

package com.threerings.presents.server;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.net.InetAddress;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.server.net.AuthingConnection;
import com.threerings.presents.server.net.PresentsConnection;

import com.threerings.nio.conman.Connection;

import static com.threerings.presents.Log.log;

/**
 * The client manager is responsible for managing client sessions which are slightly more than just
 * connections. Clients persist in the absence of connections in case a user goes bye bye
 * unintentionally and wants to reconnect and continue their session.
 *
 * <p> The client manager operates with thread safety because it is called both from the conmgr
 * thread (to notify of connections showing up or going away) and from the dobjmgr thread (when
 * clients are given the boot for application-defined reasons).
 */
@Singleton
public class ClientManager
    implements ClientResolutionListener, ReportManager.Reporter, Lifecycle.Component
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
     * Used by entities that wish to track when clients initiate and end sessions on this server.
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
     * Methods for observing additional events in the session lifecycle.
     */
    public static interface DetailedClientObserver extends ClientObserver
    {
        /**
         * Called prior to the sessions ending. Subclasses and this class tend to nuke a lot of
         * information in the process of ending. This will allow callers to act on session events
         * without creating a subclass of a session. For example lightweight management of dobj
         * fields such as flushing stats or notifying party members of a logout.
         */
        void clientSessionWillEnd (PresentsSession session);
    }

    /**
     * Constructs a client manager that will interact with the supplied connection manager.
     */
    @Inject public ClientManager (ReportManager repmgr, Lifecycle cycle)
    {
        repmgr.registerReporter(this);
        cycle.addComponent(this);
    }

    /**
     * Configures the injector we'll use to resolve dependencies for {@link PresentsSession}
     * instances.
     */
    public void setInjector (Injector injector)
    {
        _injector = injector;
    }

    /**
     * Configures the default factory for creating {@link PresentsSession} and {@link
     * ClientResolver} classes for authenticated client connections. All factories added via {@link
     * #addSessionFactory} will be offered a chance to handle sessions before this factory of last
     * resort.
     */
    public void setDefaultSessionFactory (SessionFactory factory)
    {
        _factories.set(_factories.size()-1, factory);
    }

    /**
     * Adds a session factory to the chain. This factory will be offered a chance to resolve
     * sessions before passing the buck to the next factory in the chain.
     */
    public void addSessionFactory (SessionFactory factory)
    {
        _factories.add(0, factory);
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
     * Returns all sessions logged in from the given IP in the form returned from
     * {@link InetAddress#getAddress()}.
     */
    public List<PresentsSession> getSessionsForAddress (byte[] addr)
    {
        List<PresentsSession> sessions = Lists.newArrayListWithExpectedSize(1);
        synchronized (_usermap) {
            for (PresentsSession session : _usermap.values()) {
                InetAddress sessionAddr = session.getInetAddress();
                if (sessionAddr == null) {
                    continue;
                }
                if (Arrays.equals(addr, sessionAddr.getAddress())) {
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    /**
     * Returns the number of connected clients.
     */
    public int getConnectionCount ()
    {
        return _conmap.size();
    }

    /**
     * Returns an iterable over all active client objects.
     */
    public Iterable<ClientObject> clientObjects ()
    {
        return _objmap.values();
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
        if (observer instanceof DetailedClientObserver) {
            _dclobservers.add((DetailedClientObserver)observer);
        }
    }

    /**
     * Removes an observer previously registered with {@link #addClientObserver}.
     */
    public void removeClientObserver (ClientObserver observer)
    {
        _clobservers.remove(observer);
        if (observer instanceof DetailedClientObserver) {
            _dclobservers.remove((DetailedClientObserver)observer);
        }
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
    public void applyToClient (Name username, final ClientOp clop)
    {
        resolveClientObject(username, new ClientResolutionListener() {
            public void clientResolved (Name username, ClientObject clobj) {
                try {
                    clop.apply(clobj);

                } catch (Exception e) {
                    log.warning("Client op failed", "username", username, "clop", clop, e);

                } finally {
                    releaseClientObject(username);
                }
            }

            public void resolutionFailed (Name username, Exception reason) {
                clop.resolutionFailed(reason);
            }
        });
    }

    /**
     * Requests that the client object for the specified user be resolved. <em>Note:</em> this
     * <b>must</b> be paired with a call to {@link #releaseClientObject} when the caller is
     * finished with the client object.
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

        // figure out our client resolver class
        Class<? extends ClientResolver> resolverClass = null;
        for (SessionFactory factory : _factories) {
            if ((resolverClass = factory.getClientResolverClass(username)) != null) {
                break;
            }
        }

        try {
            // create a client resolver instance which will create our client object, populate it
            // and notify the listeners
            clr = _injector.getInstance(resolverClass);
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
            log.info("Requested to release unmapped client object", "username", username);
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

    // from interface Lifecycle.Component
    public void init ()
    {
        // start up an interval that will check for and flush expired sessions (this will be
        // canceled when the omgr shuts down)
        _omgr.newInterval(new Runnable() {
            public void run () {
                flushSessions();
            }
        }).schedule(SESSION_FLUSH_INTERVAL, true);
    }

    // from interface Lifecycle.Component
    public void shutdown ()
    {
        log.info("Client manager shutting down", "ccount", _usermap.size());

        // inform all of our clients that they are being shut down
        synchronized (_usermap) {
            for (PresentsSession pc : _usermap.values()) {
                try {
                    pc.shutdown();
                } catch (Exception e) {
                    log.warning("Client choked in shutdown()",
                                "client", StringUtil.safeToString(pc), e);
                }
            }
        }
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
        PresentsConnection conn, Name authname, AuthRequest req, AuthResponse rsp)
    {
        String type = authname.getClass().getSimpleName();

        // see if a session is already registered with this name
        PresentsSession session = getClient(authname);

        if (session != null) {
            //log.info("Resuming session", "type", type, "who", authname, "conn", conn);
            session.resumeSession(req, conn);

        } else {
            log.info("Session initiated", "type", type, "who", authname, "conn", conn);
            // figure out our session class
            Class<? extends PresentsSession> sessionClass = null;
            for (SessionFactory factory : _factories) {
                if ((sessionClass = factory.getSessionClass(req)) != null) {
                    break;
                }
            }
            // create a new session and stick'em in the table
            session = _injector.getInstance(sessionClass);
            session.startSession(authname, req, conn, rsp.authdata);

            // map their session instance
            synchronized (_usermap) {
                // we refetch the authname from the session for use in the map in case it decides
                // to do something crazy like rewrite it in startSession()
                _usermap.put(session.getAuthName(), session);
            }
        }

        // map this connection to this session
        _conmap.put(conn, session);
    }

    /**
     * Called by the connection manager to let us know when a connection has failed.
     */
    public synchronized void connectionFailed (Connection conn, IOException fault)
    {
        // remove the session from the connection map
        PresentsSession session = _conmap.remove(conn);
        if (session != null) {
            log.info("Unmapped failed session", "session", session, "conn", conn, "fault", fault);
            // let the session know the connection went away
            session.wasUnmapped();
            // and let the session know things went haywire
            session.connectionFailed(fault);

        } else if (!(conn instanceof AuthingConnection)) {
            log.info("Unmapped connection failed?", "conn", conn, "fault", fault, new Exception());
        }
    }

    /**
     * Called by the connection manager to let us know when a connection has been closed.
     */
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the session from the connection map
        PresentsSession session = _conmap.remove(conn);
        if (session != null) {
            log.debug("Unmapped session", "session", session, "conn", conn);
            // let the session know the connection went away
            session.wasUnmapped();

        } else {
            log.info("Closed unmapped connection '" + conn + "'. " +
                     "Session probably not yet authenticated.");
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
     * Called by PresentsSession when it has started its session.
     */
    @EventThread
    protected void clientSessionDidStart (final PresentsSession session)
    {
        // let the observers know
        _clobservers.apply(new ObserverList.ObserverOp<ClientObserver>() {
            public boolean apply (ClientObserver observer) {
                observer.clientSessionDidStart(session);
                return true;
            }
        });
    }

    /**
     * Called by PresentsSession when it is about to end its session.
     */
    @EventThread
    protected void clientSessionWillEnd (final PresentsSession session)
    {
        // notify the observers that the session is ended
        _dclobservers.apply(new ObserverList.ObserverOp<DetailedClientObserver>() {
            public boolean apply (DetailedClientObserver observer) {
                observer.clientSessionWillEnd(session);
                return true;
            }
        });
    }

    /**
     * Called by PresentsSession when it has ended its session.
     */
    @EventThread
    protected void clientSessionDidEnd (final PresentsSession session)
    {
        // notify the observers that the session is ended
        _clobservers.apply(new ObserverList.ObserverOp<ClientObserver>() {
            public boolean apply (ClientObserver observer) {
                observer.clientSessionDidEnd(session);
                return true;
            }
        });
    }

    /**
     * Called by PresentsSession to let us know that we can clear it entirely out of the system.
     */
    protected void clearSession (PresentsSession session)
    {
        // remove the session from the username map
        PresentsSession rc;
        synchronized (_usermap) {
            rc = _usermap.remove(session.getAuthName());
        }

        // sanity check just because we can
        if (rc == null) {
            log.info("Cleared session: unregistered!", "session", session);
        } else if (rc != session) {
            log.info("Cleared session: multiple!", "s1", rc, "s2", session);
        } else {
            log.info("Cleared session", "session", session);
        }
    }

    /**
     * Called once per minute to check for sessions that have been disconnected too long and
     * forcibly end their sessions.
     */
    protected void flushSessions ()
    {
        List<PresentsSession> victims = Lists.newArrayList();
        long now = System.currentTimeMillis();

        // first build a list of our victims
        synchronized (_usermap) {
            for (PresentsSession session : _usermap.values()) {
                if (session.checkExpired(now)) {
                    victims.add(session);
                }
            }
        }

        // now end their sessions
        for (PresentsSession session : victims) {
            try {
                log.info("Session expired, ending session", "session", session,
                         "dtime", (now-session.getNetworkStamp()) + "ms].");
                session.endSession();
            } catch (Exception e) {
                log.warning("Choke while flushing session", "victim", session, e);
            }
        }
    }

    /** Used to resolve dependencies in {@link PresentsSession} instances that we create. */
    protected Injector _injector;

    /** A mapping from auth username to session instances. */
    protected Map<Name, PresentsSession> _usermap = Maps.newHashMap();

    /** A mapping from connections to session instances. */
    protected Map<Connection, PresentsSession> _conmap = Maps.newHashMap();

    /** A mapping from usernames to client object instances. */
    protected Map<Name, ClientObject> _objmap = Maps.newHashMap();

    /** A mapping of pending client resolvers. */
    protected Map<Name, ClientResolver> _penders = Maps.newHashMap();

    /** Lets us know what sort of session classes to use. */
    protected List<SessionFactory> _factories = Lists.newArrayList(SessionFactory.DEFAULT);

    /** Tracks registered {@link ClientObserver}s. */
    protected ObserverList<ClientObserver> _clobservers = ObserverList.newSafeInOrder();

    /** Tracks registered {@link DetailedClientObserver}s. */
    protected ObserverList<DetailedClientObserver> _dclobservers = ObserverList.newSafeInOrder();

    // our injected dependencies
    @Inject protected PresentsDObjectMgr _omgr;

    /** The frequency with which we check for expired sessions. */
    protected static final long SESSION_FLUSH_INTERVAL = 60 * 1000L;
}
