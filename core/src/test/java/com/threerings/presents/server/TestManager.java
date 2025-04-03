//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.util.List;

import com.google.inject.Inject;

import com.threerings.presents.client.TestService;
import com.threerings.presents.data.TestClientObject;
import com.threerings.presents.server.ClientManager;

import static com.threerings.presents.Log.log;

/**
 * Implements the server side of the TestProvider interface.
 */
public class TestManager
    implements TestProvider
{
    // from interface TestProvider
    public void getTestOid (TestClientObject caller,
        TestService.TestOidListener listener)
        throws InvocationException
    {
        log.info("Handling get test oid [src=" + caller + "].");

        // issue a test notification just for kicks
        TestSender.sendTest(caller, 1, "two");

        listener.gotTestOid(TestServer.testobj.getOid());
    }

    // from interface TestProvider
    public void test (TestClientObject caller, String one, int two, List<Integer> three,
                      TestService.TestFuncListener listener)
        throws InvocationException
    {
        log.info("Test request", "one", one, "two", two, "three", three);

        // and issue a response to this invocation request
        listener.testSucceeded(one, two);
    }

    // from interface TestProvider
    public void giveMeThePower (TestClientObject caller, TestService.ConfirmListener listener)
    {
        log.info("Giving " + caller.who() + " the power!");
        _clmgr.getClient(caller.username).setIncomingMessageThrottle(20);
        listener.requestProcessed();
    }

    @Inject protected ClientManager _clmgr;
}
