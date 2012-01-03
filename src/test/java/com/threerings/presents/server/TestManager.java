//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.server;

import java.util.List;

import com.google.inject.Inject;

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
        log.info("Test request", "one", one, "two", two, "three", three);

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
