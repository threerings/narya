//
// $Id: TestServer.java,v 1.8 2002/03/28 22:32:33 mdb Exp $

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
