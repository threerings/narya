//
// $Id: AuthingConnection.java,v 1.4 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import ninja2.core.io_core.nbio.NonblockingSocket;

import com.threerings.presents.Log;
import com.threerings.presents.net.UpstreamMessage;
import com.threerings.presents.net.AuthRequest;

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

    public String toString ()
    {
        return "[mode=AUTHING, addr=" + _socket.getInetAddress() + "]";
    }

    protected int _state = AWAITING_AUTH_REQUEST;
    protected AuthRequest _authreq;

    protected static final int AWAITING_AUTH_REQUEST = 0;
    protected static final int PROCESSING_AUTH_REQUEST = 1;
}
