//
// $Id: TestService.java,v 1.3 2001/10/02 02:05:50 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.Client;
import com.threerings.cocktail.cher.client.InvocationDirector;

/**
 * A test of the invocation services.
 */
public class TestService
{
    public static final String MODULE = "test";

    public static void test (
        Client client, String one, int two, Object rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { one, new Integer(two) };
        invdir.invoke(MODULE, "Test", args, rsptarget);
        Log.info("Sent test request [one=" + one + ", two=" + two + "].");
    }

    public static void getTestOid (Client client, Object rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[0];
        invdir.invoke(MODULE, "GetTestOid", args, rsptarget);
    }
}
