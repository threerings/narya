//
// $Id: Authenticator.java,v 1.4 2002/03/05 03:19:18 mdb Exp $

package com.threerings.presents.server.net;

import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;

/**
 * The authenticator is a pluggable component of the authentication
 * framework. The base class handles the basic mechanics of authentication
 * and a system would extend the base authenticator and add code that does
 * the actual client authentication.
 */
public abstract class Authenticator
{
    /**
     * Called by the connection manager to give us a reference to it for
     * reporting authenticated connections.
     */
    public void setConnectionManager (ConnectionManager conmgr)
    {
        _conmgr = conmgr;
    }

    /**
     * Called by the connection management code when an authenticating
     * connection has received its authentication request from the client.
     * This method must return immediately as it is called on the
     * connection manager thread. If it is possible to authenticate the
     * connection immediately, it may do so, but more likely it will fire
     * off a task on the {@link PresentsServer#invoker} to perform the
     * authentication. When the authentication is complete, the
     * authenticator implementation should call {@link
     * #connectionWasAuthenticated}.
     */
    public abstract void authenticateConnection (AuthingConnection conn);

    /**
     * This is called by authenticator implementations when they have
     * completed the authentication process. It will deliver the response
     * to the user and let the connection manager know to report the
     * authentication if it succeeded.
     */
    protected void connectionWasAuthenticated (
        AuthingConnection conn, AuthResponse rsp)
    {
        // now ship the response back
        conn.postMessage(rsp);

        // stuff a reference to the auth response into the connection so
        // that we have access to it later in the authentication process
        conn.setAuthResponse(rsp);

        // if the authentication request was granted, let the connection
        // manager know that we just authed
        if (AuthResponseData.SUCCESS.equals(rsp.getData().code)) {
            _conmgr.connectionDidAuthenticate(conn);
        }
    }

    /** The connection manager with which we're working. */
    protected ConnectionManager _conmgr;
}
