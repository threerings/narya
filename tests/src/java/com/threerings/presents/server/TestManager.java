//
// $Id$

package com.threerings.presents.server;

import java.util.List;

import com.google.inject.Inject;

import com.samskivert.util.StringUtil;

import com.threerings.presents.client.TestService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.ClientManager;

import static com.threerings.presents.Log.log;

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
        log.info("Handling get test oid [src=" + caller + "].");

        // issue a test notification just for kicks
        TestSender.sendTest(caller, 1, "two");

        listener.gotTestOid(TestServer.testobj.getOid());
    }

    // from interface TestProvider
    public void test (ClientObject caller, String one, int two, List<Integer> three,
                      TestService.TestFuncListener listener)
        throws InvocationException
    {
        log.info("Test request [one=" + one + ", two=" + two +
            ", three=" + StringUtil.toString(three) + "].");

        // and issue a response to this invocation request
        listener.testSucceeded(one, two);
    }

    // from interface TestProvider
    public void giveMeThePower (ClientObject caller, TestService.ConfirmListener listener)
    {
        log.info("Giving " + caller.who() + " the power!");
        _clmgr.getClient(caller.username).setIncomingMessageThrottle(20);
        listener.requestProcessed();
    }

    @Inject protected ClientManager _clmgr;
}
