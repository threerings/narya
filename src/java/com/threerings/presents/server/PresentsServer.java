//
// $Id: PresentsServer.java,v 1.26 2002/10/21 20:56:20 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.server.net.ConnectionManager;
import com.threerings.presents.util.Invoker;

/**
 * The presents server provides a central point of access to the various
 * facilities that make up the presents framework. To facilitate extension
 * and customization, a single instance of the presents server should be
 * created and initialized in a process. To facilitate easy access to the
 * services provided by the presents server, static references to the
 * various managers are made available in the <code>PresentsServer</code>
 * class. These will be configured when the singleton instance is
 * initialized.
 */
public class PresentsServer
{
    /** The manager of network connections. */
    public static ConnectionManager conmgr;

    /** The manager of clients. */
    public static ClientManager clmgr;

    /** The distributed object manager. */
    public static PresentsDObjectMgr omgr;

    /** The invocation manager. */
    public static InvocationManager invmgr;

    /** This is used to invoke background tasks that should not be allowed
     * to tie up the distributed object manager thread. This is generally
     * used to talk to databases and other (relatively) slow entities. */
    public static Invoker invoker;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // create our distributed object manager
        omgr = new PresentsDObjectMgr();

        // create and start up our invoker
        invoker = new Invoker(omgr);
        invoker.start();

        // create our connection manager
        conmgr = new ConnectionManager(getListenPort());
        conmgr.setAuthenticator(new DummyAuthenticator());

        // create our client manager
        clmgr = new ClientManager(conmgr);

        // create our invocation manager
        invmgr = new InvocationManager(omgr);

        // initialize the time base services
        TimeBaseProvider.init(invmgr, omgr);
    }

    /**
     * Returns the port on which the connection manager will listen for
     * client connections.
     */
    protected int getListenPort ()
    {
        return Client.DEFAULT_SERVER_PORT;
    }

    /**
     * Starts up all of the server services and enters the main server
     * event loop.
     */
    public void run ()
    {
        // post a unit that will start up the connection manager when
        // everything else in the dobjmgr queue is processed
        omgr.postUnit(new Runnable() {
            public void run () {
                // start up the connection manager
                conmgr.start();
            }
        });
        // invoke the dobjmgr event loop
        omgr.run();
    }

    /**
     * Requests that the server shut down.
     */
    public static void shutdown ()
    {
        // shut down our managers
        conmgr.shutdown();
        omgr.shutdown();
    }

    public static void main (String[] args)
    {
        Log.info("Presents server starting...");

        PresentsServer server = new PresentsServer();
        try {
            // initialize the server
            server.init();

            // check to see if we should load and invoke a test module
            // before running the server
            String testmod = System.getProperty("test_module");
            if (testmod != null) {
                try {
                    Log.info("Invoking test module [mod=" + testmod + "].");
                    Class tmclass = Class.forName(testmod);
                    Runnable trun = (Runnable)tmclass.newInstance();
                    trun.run();
                } catch (Exception e) {
                    Log.warning("Unable to invoke test module " +
                                "[mod=" + testmod + "].");
                    Log.logStackTrace(e);
                }
            }

            // start the server to running (this method call won't return
            // until the server is shut down)
            server.run();

        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
            System.exit(-1);
        }
    }
}
