//
// $Id: TestServer.java,v 1.3 2001/11/08 02:07:36 mdb Exp $

package com.threerings.parlor;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.Log;
import com.threerings.parlor.server.ParlorManager;

/**
 * A test server for the Parlor services.
 */
public class TestServer extends CrowdServer
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
