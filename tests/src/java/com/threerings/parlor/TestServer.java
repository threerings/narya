//
// $Id: TestServer.java,v 1.1 2001/10/03 03:45:44 mdb Exp $

package com.threerings.parlor.test;

import com.threerings.cocktail.party.server.PartyServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.server.ParlorManager;

/**
 * A test server for the Parlor services.
 */
public class TestServer extends PartyServer
{
    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /** Initializes the Parlor test server. */
    public void init ()
        throws Exception
    {
        super.init();

        // initialize our parlor manager
        parmgr.init(config, invmgr);

        Log.info("Parlor server initialized.");
    }

    /** Main entry point for test server. */
    public static void main (String[] args)
    {
        TestServer server = new TestServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }
}
