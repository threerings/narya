//
// $Id: ClientManager.java,v 1.29 2003/03/02 03:47:06 mdb Exp $

package com.threerings.presents.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.samskivert.util.IntervalManager;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.server.net.*;
import com.threerings.presents.server.util.SafeInterval;

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
    implements ConnectionObserver, PresentsServer.Reporter
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
        IntervalManager.register(new SafeInterval(PresentsServer.omgr) {
            public void run () {
                flushClients();
            }
        }, CLIENT_FLUSH_INTERVAL, null, true);

        // register as a "state of server" reporter
        PresentsServer.registerReporter(this);
    }

    /**
     * Instructs the client manager to construct instances of this derived
     * class of {@link PresentsClient} to managed newly accepted client
     * connections.
     */
    public void setClientClass (Class clientClass)
    {
        // sanity check
        if (!PresentsClient.class.isAssignableFrom(clientClass)) {
            Log.warning("Requested to use client class that does not " +
                        "derive from PresentsClient " +
                        "[class=" + clientClass.getName() + "].");
            return;
        }

        // make a note of it
        _clientClass = clientClass;
    }

    /**
     * Instructs the client to use instances of this {@link
     * ClientResolver} derived class when resolving clients in preparation
     * for starting a client session.
     */
    public void setClientResolverClass (Class clrClass)
    {
        // sanity check
        if (!ClientResolver.class.isAssignableFrom(clrClass)) {
            Log.warning("Requested to use client resolver class that does " +
                        "not derive from ClientResolver " +
                        "[class=" + clrClass.getName() + "].");

        } else {
            // make a note of it
            _clrClass = clrClass;
        }
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
    public PresentsClient getClient (String authUsername)
    {
        return (PresentsClient)_usermap.get(authUsername.toLowerCase());
    }

    /**
     * Returns the client object associated with the specified username.
     * This will return null unless the client object is resolved for some
     * reason (like they are logged on).
     */
    public ClientObject getClientObject (String username)
    {
        return (ClientObject)_objmap.get(toKey(username));
    }

    /**
     * We convert usernames to lower case in the username to client object
     * mapping so that we can pass arbitrarily cased usernames (like those
     * that might be typed in by a "user") straight on through.
     */
    protected final String toKey (String username)
    {
        return username.toLowerCase();
    }

    /**
     * Resolves the specified client, applies the supplied client
     * operation to them and releases the client.
     */
    public void applyToClient (String username, ClientOp clop)
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
        String username, ClientResolutionListener listener)
    {
        // look to see if the client object is already resolved
        String key = toKey(username);
        ClientObject clobj = (ClientObject)_objmap.get(key);
        if (clobj != null) {
            clobj.reference();
            listener.clientResolved(username, clobj);
            return;
        }

        // look to see if it's currently being resolved
        ClientResolver clr = (ClientResolver)_penders.get(key);
        if (clr != null) {
            // throw this guy onto the bandwagon
            clr.addResolutionListener(listener);
            return;
        }

        try {
            // create a client resolver instance which will create our
            // client object, populate it and notify the listeners
            clr = (ClientResolver)_clrClass.newInstance();
            clr.init(username);
            clr.addResolutionListener(listener);

            // request that the appropriate client object be created by
            // the dobject manager which starts the whole business off
            PresentsServer.omgr.createObject(clr.getClientObjectClass(), clr);

        } catch (Exception e) {
            // let the listener know that we're hosed
            listener.resolutionFailed(username, e);
        }
    }

    /**
     * Called by the {@link ClientResolver} once a client object has been
     * resolved.
     */
    protected synchronized void mapClientObject (
        String username, ClientObject clobj)
    {
        // stuff the object into the mapping table
        String key = toKey(username);
        _objmap.put(key, clobj);

        // and remove the resolution listener
        _penders.remove(key);
    }

    /**
     * Releases a client object that was obtained via a call to {@link
     * #resolveClientObject}. If this caller is the last reference, the
     * object will be flushed and destroyed.
     */
    public void releaseClientObject (String username)
    {
        String key = toKey(username);
        ClientObject clobj = (ClientObject)_objmap.get(key);
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
        _objmap.remove(key);

        // and destroy the object itself
        PresentsServer.omgr.destroyObject(clobj.getOid());
    }

    // documentation inherited
    public synchronized void connectionEstablished (
        Connection conn, AuthRequest req, AuthResponse rsp)
    {
        Credentials creds = req.getCredentials();
        String username = creds.getUsername().toLowerCase();

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
            try {
                // create a client and start up its session
                client = (PresentsClient)_clientClass.newInstance();
                client.startSession(this, creds, conn);

                // map their client instance
                _usermap.put(username, client);

            } catch (Exception e) {
                Log.warning("Failed to instantiate client instance to " +
                            "manage new client connection '" + conn + "'.");
                Log.logStackTrace(e);
            }
        }

        // map this connection to this client
        _conmap.put(conn, client);
    }

    // documentation inherited
    public synchronized void connectionFailed (
        Connection conn, IOException fault)
    {
        // remove the client from the connection map
        PresentsClient client = (PresentsClient)_conmap.remove(conn);
        if (client != null) {
            Log.info("Unmapped failed client [client=" + client +
                     ", conn=" + conn + ", fault=" + fault + "].");
            // let the client know the connection went away
            client.wasUnmapped();
            // and let the client know things went haywire
            client.connectionFailed(fault);

        } else {
            Log.info("Unmapped connection failed? [conn=" + conn +
                     ", fault=" + fault + "].");
            Thread.dumpStack();
        }
    }

    // documentation inherited
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the client from the connection map
        PresentsClient client = (PresentsClient)_conmap.remove(conn);
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
    public void appendReport (StringBuffer report, long now, long sinceLast)
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
        Credentials creds = client.getCredentials();
        String username = client.getUsername();

        // remove the client from the username map
        PresentsClient rc = (PresentsClient)
            _usermap.remove(creds.getUsername().toLowerCase());

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
        ArrayList victims = null;
        long now = System.currentTimeMillis();

        // first build a list of our victims (we can't flush clients
        // directly while iterating due to risk of a
        // ConcurrentModificationException)
        Iterator iter = _usermap.values().iterator();
        while (iter.hasNext()) {
            PresentsClient client = (PresentsClient)iter.next();
            if (client.checkExpired(now)) {
                if (victims == null) {
                    victims = new ArrayList();
                }
                victims.add(client);
            }
        }

        if (victims != null) {
            for (int ii = 0; ii < victims.size(); ii++) {
                PresentsClient client = (PresentsClient)victims.get(ii);
                Log.info("Client expired, ending session [client=" + client +
                         ", dtime=" + (now-client.getNetworkStamp()) + "ms].");
                client.endSession();
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
        public void clientResolved (String username, ClientObject clobj)
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
        public void resolutionFailed (String username, Exception reason)
        {
            _clop.resolutionFailed(reason);
        }

        protected ClientOp _clop;
    }

    /** A mapping from auth username to client instances. */
    protected HashMap _usermap = new HashMap();

    /** A mapping from connections to client instances. */
    protected HashMap _conmap = new HashMap();

    /** A mapping from usernames to client object instances. */
    protected HashMap _objmap = new HashMap();

    /** A mapping of pending client resolvers. */
    protected HashMap _penders = new HashMap();

    /** A set containing the usernames of all locked clients. */
    protected HashSet _locks = new HashSet();

    /** The client class in use. */
    protected Class _clientClass = PresentsClient.class;

    /** The client resolver class in use. */
    protected Class _clrClass = ClientResolver.class;

    /** The frequency with which we check for expired clients. */
    protected static final long CLIENT_FLUSH_INTERVAL = 60 * 1000L;
}
