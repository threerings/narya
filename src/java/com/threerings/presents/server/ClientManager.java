//
// $Id: ClientManager.java,v 1.1 2001/06/01 22:12:03 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.io.IOException;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.server.net.*;

/**
 * The client manager is responsible for managing the clients (surprise,
 * surprise) which are slightly more than just connections. Clients
 * persist in the absence of connections in case a user goes bye bye
 * unintentionally and wants to reconnect and continue their session.
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
    public void connectionEstablished (Connection conn)
    {
        Log.info("Connection established: " + conn);
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
    public void connectionFailed (Connection conn, IOException fault)
    {
        Log.info("Connection failed: " + conn + ": " + fault);
    }

    /**
     * Called when a connection has been closed in an orderly manner.
     *
     * @param conn The recently closed connection.
     */
    public void connectionClosed (Connection conn)
    {
        Log.info("Connection closed: " + conn);
    }
}
