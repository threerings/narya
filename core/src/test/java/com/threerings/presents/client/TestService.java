//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import java.util.List;

import com.threerings.presents.data.TestClientObject;

/**
 * A test of the invocation services.
 */
public interface TestService extends InvocationService<TestClientObject>
{
    /** Used to dispatch responses to {@link TestService#test} requests. */
    public static interface TestFuncListener extends InvocationListener
    {
        /** Informs listener of successful {@link TestService#test} request. */
        public void testSucceeded (String one, int two);
    }

    /** Used to dispatch responses to {@link TestService#getTestOid} requests. */
    public static interface TestOidListener extends InvocationListener
    {
        /** Communicates test oid to listener. */
        public void gotTestOid (int testOid);
    }

    /** Issues a test request. */
    public void test (String one, int two, List<Integer> three,
                      TestFuncListener listener);

    /** Issues a request for the test oid. */
    public void getTestOid (TestOidListener listener);

    /** Tests upping the client's maximum message rate. */
    public void giveMeThePower (ConfirmListener listener);
}
