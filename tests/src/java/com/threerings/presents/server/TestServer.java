//
// $Id: TestServer.java,v 1.5 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.server.test;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.server.*;

public class TestServer extends PresentsServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "test";

    public static TestObject testobj;

    public void init ()
        throws Exception
    {
        super.init();

        // bind the crowd server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH, true);

        // register our invocation service providers
        registerProviders(config.getValue(PROVIDERS_KEY, (String[])null));

        // create a test object
        Subscriber sub = new Subscriber()
        {
            public void objectAvailable (DObject object)
            {
                testobj = (TestObject)object;
            }

            public void requestFailed (int oid, ObjectAccessException cause)
            {
                Log.warning("Unable to create test object " +
                            "[error=" + cause + "].");
            }
        };
        omgr.createObject(TestObject.class, sub, false);
    }

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

    // the path to the config file
    protected final static String CONFIG_PATH =
        "rsrc/config/presents/test/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";
}
