//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.util.List;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.TestService;
import com.threerings.presents.data.TestClientObject;

/**
 * Defines the server-side of the {@link TestService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TestService.java.")
public interface TestProvider extends InvocationProvider
{
    /**
     * Handles a {@link TestService#getTestOid} request.
     */
    void getTestOid (TestClientObject caller, TestService.TestOidListener arg1)
        throws InvocationException;

    /**
     * Handles a {@link TestService#giveMeThePower} request.
     */
    void giveMeThePower (TestClientObject caller, InvocationService.ConfirmListener arg1)
        throws InvocationException;

    /**
     * Handles a {@link TestService#test} request.
     */
    void test (TestClientObject caller, String arg1, int arg2, List<Integer> arg3, TestService.TestFuncListener arg4)
        throws InvocationException;
}
