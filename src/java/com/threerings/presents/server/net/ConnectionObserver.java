//
// $Id: ConnectionObserver.java,v 1.4 2001/06/02 01:30:37 mdb Exp $

package com.threerings.cocktail.cher.server.net;

import java.io.IOException;
import com.threerings.cocktail.cher.net.Credentials;

/**
 * A connection observer can be registered with the connection manager to
 * hear about new connections and to be notified when connections fail or
 * are closed. Only fully authenticated connections will be passed on to
 * the connection observer. Connections that fail to authenticate will be
 * handled entirely within the confines of the connection manager.
 *
 * @see ConnectionManager
 * @see Connection
 */
public interface ConnectionObserver
{
    /**
     * Called when a new connection is established with the connection
     * manager. Only fully authenticated connections will be passed on to
     * the connection observer.
     *
     * @param conn The newly established connection.
     * @param creds The credentials with which this connection
     * (successfully) authenticated.
     */
    public void connectionEstablished (Connection conn, Credentials creds);

    /**
     * Called if a connection fails for any reason. If a connection fails,
     * <code>connectionClosed</code> will not be called. This call to
     * <code>connectionFailed</code> is the last the observers will hear
     * about it.
     *
     * @param conn The connection in that failed.
     * @param fault The exception associated with the failure.
     */
    public void connectionFailed (Connection conn, IOException fault);

    /**
     * Called when a connection has been closed in an orderly manner.
     *
     * @param conn The recently closed connection.
     */
    public void connectionClosed (Connection conn);
}
