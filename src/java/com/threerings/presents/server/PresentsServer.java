//
// $Id: PresentsServer.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.server;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.server.net.AuthManager;
import com.threerings.cocktail.cher.server.net.ConnectionManager;

/**
 * The cher server provides a central point of access to the various
 * facilities that make up the cher layer of the system.
 */
public class CherServer
{
    /** The authentication manager. */
    public static AuthManager authmgr;

    /** The manager of network connections. */
    public static ConnectionManager cmgr;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public static void init ()
    {
        try {
            // create our authentication manager
            authmgr = new AuthManager(new DummyAuthenticator());
            // create our connection manager
            cmgr = new ConnectionManager(authmgr);

        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    /**
     * Starts up all of the server services and enters the main server
     * event loop.
     */
    public static void start ()
    {
        // start up the auth manager
        authmgr.start();
        // start up the connection manager
        cmgr.start();

        // for now, just block because we've nothing to do
        try {
            synchronized (CherServer.class) {
                CherServer.class.wait();
            }
        } catch (InterruptedException ie) {
        }
    }

    public static void main (String[] args)
    {
        init();
        start();
    }
}
