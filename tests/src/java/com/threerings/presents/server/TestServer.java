//
// $Id: TestServer.java,v 1.9 2002/08/14 19:08:01 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

public class TestServer extends PresentsServer
{
    public static TestObject testobj;

    public void init ()
        throws Exception
    {
        super.init();

        // register our test provider
        invmgr.registerDispatcher(new TestDispatcher(new TestProvider()), true);

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
        omgr.createObject(TestObject.class, sub);
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
}
