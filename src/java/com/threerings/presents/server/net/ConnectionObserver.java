//
// $Id: ConnectionObserver.java,v 1.6 2001/12/03 20:14:51 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;

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
     * @param req The auth request provided by the client.
     * @param rsp The auth response provided to the client.
     */
    public void connectionEstablished (
        Connection conn, AuthRequest req, AuthResponse rsp);

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
