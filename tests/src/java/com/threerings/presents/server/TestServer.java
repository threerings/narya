//
// $Id: TestServer.java,v 1.1 2001/08/07 20:38:58 mdb Exp $

package com.threerings.cocktail.cher.server.test;

import java.io.IOException;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.server.*;

public class TestServer extends CherServer
{
    /** The namespace used for server config properties. */
    public static final String CONFIG_KEY = "test";

    public static TestObject testobj;

    public void init ()
        throws IOException
    {
        super.init();

        // bind the party server config into the namespace
        config.bindProperties(CONFIG_KEY, CONFIG_PATH);

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

            public boolean handleEvent (DEvent event, DObject target)
            {
                return false;
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
        "rsrc/config/cocktail/cher/test/server";

    // the config key for our list of invocation provider mappings
    protected final static String PROVIDERS_KEY = CONFIG_KEY + ".providers";
}
