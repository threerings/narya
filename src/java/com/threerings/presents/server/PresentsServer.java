//
// $Id: PresentsServer.java,v 1.14 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.Config;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.server.net.AuthManager;
import com.threerings.presents.server.net.ConnectionManager;

import com.threerings.presents.server.test.TestObject;
import com.threerings.presents.server.test.TestProvider;
import com.threerings.presents.client.test.TestService;

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
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "presents";

    /** The server configuration. */
    public static Config config;

    /** The authentication manager. */
    public static AuthManager authmgr;

    /** The manager of network connections. */
    public static ConnectionManager conmgr;

    /** The manager of clients. */
    public static ClientManager clmgr;

    /** The distributed object manager. */
    public static PresentsDObjectMgr omgr;

    /** The invocation manager. */
    public static InvocationManager invmgr;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // create our configuration object
        config = new Config();
        // bind the presents server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH, true);

        // create our authentication manager
        authmgr = new AuthManager(new DummyAuthenticator());
        // create our connection manager
        conmgr = new ConnectionManager(config, authmgr);
        // create our client manager
        clmgr = new ClientManager(conmgr);
        // create our distributed object manager
        omgr = new PresentsDObjectMgr();
        // create our invocation manager
        invmgr = new InvocationManager(omgr);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));
    }

    /**
     * Registers invocation service providers as parsed from a
     * configuration file. Each string in the array should contain an
     * expression of the form:
     *
     * <pre>
     * module = provider fully-qualified class name
     * </pre>
     *
     * A comma separated list of these can be specified in the
     * configuration file and loaded into a string array easily. These
     * providers will be instantiated and registered with the invocation
     * manager.
     */
    protected void registerProviders (String[] providers)
    {
        // ignore null arrays to make life easier for the caller
        if (providers == null) {
            return;
        }

        for (int i = 0; i < providers.length; i++) {
            int eidx = providers[i].indexOf("=");
            if (eidx == -1) {
                Log.warning("Ignoring bogus provider declaration " +
                            "[decl=" + providers[i] + "].");
                continue;
            }

            String module = providers[i].substring(0, eidx).trim();
            String pname = providers[i].substring(eidx+1).trim();

            // instantiate the provider class and register it
            try {
                Class pclass = Class.forName(pname);
                InvocationProvider provider = (InvocationProvider)
                    pclass.newInstance();
                invmgr.registerProvider(module, provider);
                Log.info("Registered provider [module=" + module +
                         ", provider=" + pname + "].");

            } catch (Exception e) {
                Log.warning("Unable to register provider [module=" + module +
                            ", provider=" + pname + ", error=" + e + "].");
            }
        }
    }

    /**
     * Starts up all of the server services and enters the main server
     * event loop.
     */
    public void run ()
    {
        // start up the auth manager
        authmgr.start();
        // start up the connection manager
        conmgr.start();
        // invoke the dobjmgr event loop
        omgr.run();
    }

    /**
     * Requests that the server shut down.
     */
    public static void shutdown ()
    {
        // shut down our managers
        authmgr.shutdown();
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
        }
    }

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/presents/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";
}
