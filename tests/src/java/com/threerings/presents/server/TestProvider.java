//
// $Id: TestProvider.java,v 1.4 2001/07/19 19:18:07 mdb Exp $

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
    public Object[] handleTestRequest (
        ClientObject source, String one, int two)
    {
        Log.info("Test request [one=" + one + ", two=" + two + "].");

        // issue a test notification just for kicks
        Object[] args = new Object[] { new Integer(1), "two" };
        CherServer.invmgr.sendNotification(
            source.getOid(), TestService.MODULE, "Test", args);

        // and issue a response to this invocation request
        return createResponse("TestSucceeded", one, new Integer(two));
    }
}
