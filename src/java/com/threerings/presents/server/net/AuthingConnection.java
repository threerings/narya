//
// $Id: AuthingConnection.java,v 1.5 2001/12/03 20:14:51 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import ninja2.core.io_core.nbio.NonblockingSocket;

import com.threerings.presents.Log;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.UpstreamMessage;

/**
 * The authing connection manages the client connection until
 * authentication has completed (for better or for worse).
 */
public class AuthingConnection
    extends Connection implements MessageHandler
{
    /**
     * Creates a new authing connection object that will manage the
     * authentication process for the suppled client socket.
     */
    public AuthingConnection (ConnectionManager cmgr,
                              NonblockingSocket socket)
        throws IOException
    {
        super(cmgr, socket);
        // we are our own message handler
        setMessageHandler(this);
    }

    /**
     * Called when a new message has arrived from the client.
     */
    public void handleMessage (UpstreamMessage msg)
    {
        try {
            // keep a handle on our auth request
            _authreq = (AuthRequest)msg;

            // post ourselves for processing by the authmgr
            _cmgr.getAuthManager().postAuthingConnection(this);

        } catch (ClassCastException cce) {
            Log.warning("Received non-authreq message during " +
                        "authentication process [conn=" + this +
                        ", msg=" + msg + "].");
        }
    }

    /**
     * Returns a reference to the auth request currently being processed
     * by this authing connection.
     */
    public AuthRequest getAuthRequest ()
    {
        return _authreq;
    }

    /**
     * Returns the auth response delivered to the client (only valid after
     * the auth request has been processed.
     */
    public AuthResponse getAuthResponse ()
    {
        return _authrsp;
    }

    /**
     * Stores a reference to the auth response delivered to this
     * connection. This is called by the auth manager after delivering the
     * auth response to the client.
     */
    public void setAuthResponse (AuthResponse authrsp)
    {
        _authrsp = authrsp;
    }

    public String toString ()
    {
        return "[mode=AUTHING, addr=" + _socket.getInetAddress() + "]";
    }

    protected AuthRequest _authreq;
    protected AuthResponse _authrsp;
}
