//
// $Id: TestProvider.java,v 1.7 2001/08/11 00:12:11 mdb Exp $

package com.threerings.cocktail.cher.server.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.test.TestService;
import com.threerings.cocktail.cher.data.ClientObject;
import com.threerings.cocktail.cher.server.CherServer;
import com.threerings.cocktail.cher.server.InvocationProvider;

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
        CherServer.invmgr.sendNotification(
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
