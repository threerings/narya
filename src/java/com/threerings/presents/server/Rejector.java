//
// $Id$

package com.threerings.presents.server;

import com.threerings.util.MessageBundle;

import com.threerings.presents.Log;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.net.AuthingConnection;

/**
 * A simple server that does nothing more than spit out a canned error
 * response to everyone who logs in.
 */
public class Rejector extends PresentsServer
{
    // documentation inherited
    public void init ()
        throws Exception
    {
        super.init();
        conmgr.setAuthenticator(new RejectingAuthenticator());
    }

    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: Rejector error_msg [args]");
            System.exit(-1);
        }

        _errmsg = args[0];
        if (args.length > 1) {
            String[] eargs = new String[args.length-1];
            System.arraycopy(args, 1, eargs, 0, eargs.length);
            _errmsg = MessageBundle.tcompose(_errmsg, eargs);
        }

        Rejector server = new Rejector();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    /**
     * An authenticator implementation that refuses all authentication
     * requests.
     */
    protected class RejectingAuthenticator extends Authenticator
    {
        /** Reject all authentication requests. */
        public void authenticateConnection (AuthingConnection conn)
        {
            Log.info("Rejecting request: " + conn.getAuthRequest());
            AuthResponseData rdata = new AuthResponseData();
            rdata.code = _errmsg;
            connectionWasAuthenticated(conn, new AuthResponse(rdata));
        }
    }

    protected static String _errmsg;
}
