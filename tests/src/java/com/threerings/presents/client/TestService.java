//
// $Id: TestService.java,v 1.6 2002/08/14 19:07:59 mdb Exp $

package com.threerings.presents.client;

/**
 * A test of the invocation services.
 */
public interface TestService extends InvocationService
{
    /** Used to dispatch responses to {@link #test} requests. */
    public static interface TestFuncListener extends InvocationListener
    {
        /** Informs listener of successful {@link #test} request. */
        public void testSucceeded (String one, int two);
    }

    /** Issues a test request. */
    public void test (
        Client client, String one, int two, TestFuncListener listener);

    /** Used to dispatch responses to {@link #getTestOid} requests. */
    public static interface TestOidListener extends InvocationListener
    {
        /** Communicates test oid to listener. */
        public void gotTestOid (int testOid);
    }

    /** Issues a request for the test oid. */
    public void getTestOid (Client client, TestOidListener listener);
}
