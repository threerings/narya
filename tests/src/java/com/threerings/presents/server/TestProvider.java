//
// $Id: TestProvider.java,v 1.11 2002/08/14 19:08:00 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.client.TestService;
import com.threerings.presents.client.TestService.TestFuncListener;
import com.threerings.presents.client.TestService.TestOidListener;
import com.threerings.presents.data.ClientObject;

/**
 * A test of the invocation services.
 */
public class TestProvider implements InvocationProvider
{
    public void test (
        ClientObject caller, String one, int two, TestFuncListener listener)
    {
        Log.info("Test request [one=" + one + ", two=" + two + "].");

        // issue a test notification just for kicks
        TestSender.sendTest(caller, 1, "two");

        // and issue a response to this invocation request
        listener.testSucceeded(one, two);
    }

    public void getTestOid (ClientObject caller, TestOidListener listener)
    {
        Log.info("Handling get test oid [src=" + caller + "].");

        // issue a test notification just for kicks
        TestSender.sendTest(caller, 1, "two");

        listener.gotTestOid(TestServer.testobj.getOid());
    }
}
