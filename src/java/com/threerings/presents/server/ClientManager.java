//
// $Id: ClientManager.java,v 1.3 2001/06/05 22:44:31 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.io.IOException;
import java.util.HashMap;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.net.Credentials;
import com.threerings.cocktail.cher.server.net.*;

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
    public ClientManager (ConnectionManager conmgr)
    {
        // register ourselves as a connection observer
        conmgr.addConnectionObserver(this);
    }

    /**
     * Called when a new connection is established with the connection
     * manager. Only fully authenticated connections will be passed on to
     * the connection observer.
     *
     * @param conn The newly established connection.
     */
    public synchronized
        void connectionEstablished (Connection conn, Credentials creds)
    {
        String username = creds.getUsername();

        // see if there's a client already registered with this username
        Client client = (Client)_usermap.get(username);

        if (client != null) {
            Log.info("Session resumed [username=" + username +
                     ", conn=" + conn + "].");
            client.resumeSession(conn);

        } else {
            Log.info("Session initiated [username=" + username +
                     ", conn=" + conn + "].");
            // create a new client and stick'em in the table
            client = new Client(this, username, conn);
            _usermap.put(username, conn);
        }
    }

    /**
     * Called if a connection fails for any reason. If a connection fails,
     * <code>connectionClosed</code> will not be called. This call to
     * <code>connectionFailed</code> is the last the observers will hear
     * about it.
     *
     * @param conn The connection in that failed.
     * @param fault The exception associated with the failure.
     */
    public synchronized
        void connectionFailed (Connection conn, IOException fault)
    {
        // remove the client from the connection map
        Client client = (Client)_connmap.remove(conn);
        if (client != null) {
            Log.info("Unmapped failed client [client=" + client +
                     ", conn=" + conn + ", fault=" + fault + "].");
            // let the client know things went haywire
            client.connectionFailed(fault);

        } else {
            Log.info("Unmapped connection failed? [conn=" + conn +
                     ", fault=" + fault + "].");
        }
    }

    /**
     * Called when a connection has been closed in an orderly manner.
     *
     * @param conn The recently closed connection.
     */
    public synchronized void connectionClosed (Connection conn)
    {
        // remove the client from the connection map
        Client client = (Client)_connmap.remove(conn);
        if (client != null) {
            Log.info("Unmapped client [client=" + client +
                     ", conn=" + conn + "].");
        } else {
            Log.info("Closed unmapped connection? [conn=" + conn + "].");
        }
    }

    /**
     * Called by the client instance when the client requests a logoff.
     * This is called from the conmgr thread.
     */
    synchronized void clientDidEndSession (Client client)
    {
    }

    protected HashMap _usermap = new HashMap();
    protected HashMap _connmap = new HashMap();
}
