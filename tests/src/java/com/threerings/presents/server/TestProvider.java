//
// $Id: TestProvider.java,v 1.9 2001/11/08 02:07:36 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.client.test.TestService;
import com.threerings.presents.data.ClientObject;

/**
 * A test of the invocation services.
 */
public class TestProvider extends InvocationProvider
{
    public void handleTestRequest (
        ClientObject source, int invid, String one, int two)
    {
        Log.info("Test request [one=" + one + ", two=" + two + "].");

        // issue a test notification just for kicks
        Object[] args = new Object[] { new Integer(1), "two" };
        PresentsServer.invmgr.sendNotification(
            source.getOid(), TestService.MODULE, "Test", args);

        // and issue a response to this invocation request
        sendResponse(source, invid, "TestSucceeded", one, new Integer(two));
    }

    public void handleGetTestOidRequest (ClientObject source, int invid)
    {
        Log.info("Handling get test oid [src=" + source.getOid() +
                 ", invid=" + invid + "].");
        int oid = TestServer.testobj.getOid();
        sendResponse(source, invid, "GotTestOid", new Integer(oid));
    }
}
