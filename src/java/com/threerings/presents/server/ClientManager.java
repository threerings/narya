//
// $Id: ClientManager.java,v 1.17 2002/04/18 22:37:08 mdb Exp $

package com.threerings.presents.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

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
public class ClientManager implements ConnectionObserver
{
    /**
     * Constructs a client manager that will interact with the supplied
     * connection manager.
     */
    public ClientManager (ConnectionManager conmgr)
    {
        // register ourselves as a connection observer
        conmgr.addConnectionObserver(this);
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
     * specified username or null if that client is not currently
     * connected to the server.
     */
    public PresentsClient getClient (String username)
    {
        return (PresentsClient)_usermap.get(username);
    }

    /**
     * Returns the client object associated with the specified username.
     * This will return null unless the client object is resolved for some
     * reason (like they are logged on).
     */
    public ClientObject getClientObject (String username)
    {
        return (ClientObject)_objmap.get(username);
    }

    /**
     * Requests that the client object for the specified user be resolved.
     * If the client object is already resolved, the request will be
     * processed immediately, otherwise the appropriate client object will
     * be instantiated and populated by the registered client resolver
     * (which may involve talking to databases).
     */
    public synchronized void resolveClientObject (
        String username, ClientResolutionListener listener)
    {
        // look to see if the client object is already resolved
        ClientObject clobj = (ClientObject)_objmap.get(username);
        if (clobj != null) {
            listener.clientResolved(username, clobj);
            return;
        }

        // look to see if it's currently being resolved
        ClientResolver clr = (ClientResolver)_penders.get(username);
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
        _objmap.put(username, clobj);

        // and remove the resolution listener
        _penders.remove(username);
    }

    /**
     * If an entity resolves a client object outside the scope of a normal
     * client session, it should call this to unmap the client object when
     * it's finished. This won't actually unmap the client if someone came
     * along and started a session for that client in the meanwhile, but
     * if it does unmap the client it will also destroy the client object
     * (finishing the job, as it were).
     */
    public synchronized void unmapClientObject (String username)
    {
        // we only remove the mapping if there's not a session in progress
        // (which is indicated by a mapping in the usermap table)
        if (!_usermap.containsKey(username)) {
            ClientObject clobj = (ClientObject)_objmap.remove(username);
            PresentsServer.omgr.destroyObject(clobj.getOid());
        }
    }

    // documentation inherited
    public synchronized void connectionEstablished (
        Connection conn, AuthRequest req, AuthResponse rsp)
    {
        Credentials creds = req.getCredentials();
        String username = creds.getUsername();

        // see if there's a client already registered with this username
        PresentsClient client = (PresentsClient)_usermap.get(username);

        if (client != null) {
            Log.info("Session resumed [username=" + username +
                     ", conn=" + conn + "].");
            client.resumeSession(conn);

        } else {
            Log.info("Session initiated [username=" + username +
                     ", conn=" + conn + "].");
            // create a new client and stick'em in the table
            try {
                client = (PresentsClient)_clientClass.newInstance();
                client.startSession(this, username, conn);
                _usermap.put(username, client);

            } catch (Exception e) {
                Log.warning("Failed to instantiate client instance to " +
                            "manage new client connection " +
                            "[conn=" + conn + "].");
                Log.logStackTrace(e);
            }
        }

        // map this connection to this client
        _conmap.put(conn, client);
    }

    // documentation inherited
    public synchronized
        void connectionFailed (Connection conn, IOException fault)
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
        }
    }

    // documentation inherited
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the client from the connection map
        PresentsClient client = (PresentsClient)_conmap.remove(conn);
        if (client != null) {
            Log.info("Unmapped client [client=" + client +
                     ", conn=" + conn + "].");
            // let the client know the connection went away
            client.wasUnmapped();

        } else {
            Log.info("Closed unmapped connection? [conn=" + conn + "].");
        }
    }

    /**
     * Called by the client instance when the client requests a logoff.
     * This is called from the conmgr thread.
     */
    synchronized void clientDidEndSession (PresentsClient client)
    {
        String username = client.getUsername();
        // remove the client from the username map
        PresentsClient rc = (PresentsClient)_usermap.remove(username);
        // and the client object mapping as well
        _objmap.remove(username);

        // sanity check just because we can
        if (rc == null) {
            Log.warning("Unregistered client ended session " +
                        "[client=" + client + "].");
        } else if (rc != client) {
            Log.warning("Different clients with same username!? " +
                        "[c1=" + rc + ", c2=" + client + "].");
        } else {
            Log.info("Ending session [client=" + client + "].");
        }
    }

    /** A mapping from usernames to client instances. */
    protected HashMap _usermap = new HashMap();

    /** A mapping from connections to client instances. */
    protected HashMap _conmap = new HashMap();

    /** A mapping from usernames to client object instances. */
    protected HashMap _objmap = new HashMap();

    /** A mapping of pending client resolvers. */
    protected HashMap _penders = new HashMap();

    /** The client class in use. */
    protected Class _clientClass = PresentsClient.class;

    /** The client resolver class in use. */
    protected Class _clrClass = ClientResolver.class;
}
