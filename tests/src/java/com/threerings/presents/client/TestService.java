//
// $Id: TestService.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.client.test;

import com.threerings.presents.Log;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

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
