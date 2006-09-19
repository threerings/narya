//
// $Id$

package com.threerings.presents.server;

import java.util.ArrayList;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.client.TestService;
import com.threerings.presents.data.ClientObject;

/**
 * Implements the server side of the TestProvider interface.
 */
public class TestManager
    implements TestProvider
{
    // from interface TestProvider
    public void getTestOid (ClientObject caller,
        TestService.TestOidListener listener)
        throws InvocationException
    {
        Log.info("Handling get test oid [src=" + caller + "].");

        // issue a test notification just for kicks
        TestSender.sendTest(caller, 1, "two");

        listener.gotTestOid(TestServer.testobj.getOid());
    }

    // from interface TestProvider
    public void test (ClientObject caller, String one, int two,
        ArrayList<Integer> three, TestService.TestFuncListener listener)
        throws InvocationException
    {
        Log.info("Test request [one=" + one + ", two=" + two +
            ", three=" + StringUtil.toString(three) + "].");

        // and issue a response to this invocation request
        listener.testSucceeded(one, two);
    }
}
