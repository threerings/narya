//
// $Id: TestService.java,v 1.2 2001/08/07 20:38:58 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationManager;

/**
 * A test of the invocation services.
 */
public class TestService
{
    public static final String MODULE = "test";

    public static void test (
        Client client, String one, int two, Object rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[] { one, new Integer(two) };
        invmgr.invoke(MODULE, "Test", args, rsptarget);
        Log.info("Sent test request [one=" + one + ", two=" + two + "].");
    }

    public static void getTestOid (Client client, Object rsptarget)
    {
        InvocationManager invmgr = client.getInvocationManager();
        Object[] args = new Object[0];
        invmgr.invoke(MODULE, "GetTestOid", args, rsptarget);
    }
}
